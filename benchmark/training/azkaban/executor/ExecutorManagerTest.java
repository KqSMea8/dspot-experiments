/**
 * Copyright 2017 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package azkaban.executor;


import ConfigurationKeys.EXECUTOR_PORT;
import ConnectorParams.EXECUTE_ACTION;
import Constants.ConfigurationKeys.MAX_DISPATCHING_ERRORS_PERMITTED;
import Constants.ConfigurationKeys.USE_MULTIPLE_EXECUTORS;
import ExecutionOptions.CONCURRENT_OPTION_SKIP;
import Status.FAILED;
import azkaban.alert.Alerter;
import azkaban.metrics.CommonMetrics;
import azkaban.user.User;
import azkaban.utils.Pair;
import azkaban.utils.Props;
import azkaban.utils.TestUtils;
import com.codahale.metrics.MetricRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


/**
 * Test class for executor manager
 */
public class ExecutorManagerTest {
    private final Map<Integer, Pair<ExecutionReference, ExecutableFlow>> activeFlows = new HashMap<>();

    private final CommonMetrics commonMetrics = new CommonMetrics(new azkaban.metrics.MetricsManager(new MetricRegistry()));

    private ExecutorManager manager;

    private ExecutorLoader loader;

    private Props props;

    private User user;

    private ExecutableFlow flow1;

    private ExecutableFlow flow2;

    private AlerterHolder alertHolder;

    private ExecutorApiGateway apiGateway;

    private Alerter mailAlerter;

    private RunningExecutions runningExecutions;

    private ExecutorManagerUpdaterStage updaterStage;

    /* Test create an executor manager instance without any executor local or
    remote
     */
    @Test(expected = ExecutorManagerException.class)
    public void testNoExecutorScenario() throws Exception {
        this.props.put(USE_MULTIPLE_EXECUTORS, "true");
        @SuppressWarnings("unused")
        final ExecutorManager manager = createExecutorManager();
    }

    /* Test error message with unsupported local executor conf */
    @Test
    public void testLocalExecutorScenario() {
        this.props.put(EXECUTOR_PORT, 12345);
        final Throwable thrown = catchThrowable(() -> createExecutorManager());
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
        assertThat(thrown.getMessage()).isEqualTo("azkaban.use.multiple.executors must be true. Single executor mode is not supported any more.");
    }

    /* Test executor manager initialization with multiple executors */
    @Test
    public void testMultipleExecutorScenario() throws Exception {
        this.props.put(USE_MULTIPLE_EXECUTORS, "true");
        final Executor executor1 = this.loader.addExecutor("localhost", 12345);
        final Executor executor2 = this.loader.addExecutor("localhost", 12346);
        final ExecutorManager manager = createExecutorManager();
        final Set<Executor> activeExecutors = new HashSet(manager.getAllActiveExecutors());
        Assert.assertArrayEquals(activeExecutors.toArray(), new Executor[]{ executor1, executor2 });
    }

    /* Test executor manager active executor reload */
    @Test
    public void testSetupExecutorsSucess() throws Exception {
        this.props.put(USE_MULTIPLE_EXECUTORS, "true");
        final Executor executor1 = this.loader.addExecutor("localhost", 12345);
        final ExecutorManager manager = createExecutorManager();
        Assert.assertArrayEquals(manager.getAllActiveExecutors().toArray(), new Executor[]{ executor1 });
        // mark older executor as inactive
        executor1.setActive(false);
        this.loader.updateExecutor(executor1);
        final Executor executor2 = this.loader.addExecutor("localhost", 12346);
        final Executor executor3 = this.loader.addExecutor("localhost", 12347);
        manager.setupExecutors();
        Assert.assertArrayEquals(manager.getAllActiveExecutors().toArray(), new Executor[]{ executor2, executor3 });
    }

    /* Test executor manager active executor reload and resulting in no active
    executors
     */
    @Test(expected = ExecutorManagerException.class)
    public void testSetupExecutorsException() throws Exception {
        this.props.put(USE_MULTIPLE_EXECUTORS, "true");
        final Executor executor1 = this.loader.addExecutor("localhost", 12345);
        final ExecutorManager manager = createExecutorManager();
        final Set<Executor> activeExecutors = new HashSet(manager.getAllActiveExecutors());
        Assert.assertArrayEquals(activeExecutors.toArray(), new Executor[]{ executor1 });
        // mark older executor as inactive
        executor1.setActive(false);
        this.loader.updateExecutor(executor1);
        manager.setupExecutors();
    }

    /* Test disabling queue process thread to pause dispatching */
    @Test
    public void testDisablingQueueProcessThread() throws Exception {
        final ExecutorManager manager = createMultiExecutorManagerInstance();
        manager.enableQueueProcessorThread();
        Assert.assertEquals(manager.isQueueProcessorThreadActive(), true);
        manager.disableQueueProcessorThread();
        Assert.assertEquals(manager.isQueueProcessorThreadActive(), false);
    }

    /* Test renabling queue process thread to pause restart dispatching */
    @Test
    public void testEnablingQueueProcessThread() throws Exception {
        final ExecutorManager manager = createMultiExecutorManagerInstance();
        Assert.assertEquals(manager.isQueueProcessorThreadActive(), false);
        manager.enableQueueProcessorThread();
        Assert.assertEquals(manager.isQueueProcessorThreadActive(), true);
    }

    /* Test submit a non-dispatched flow */
    @Test
    public void testQueuedFlows() throws Exception {
        final ExecutorManager manager = createMultiExecutorManagerInstance();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlow("exectest1", "exec1");
        flow1.setExecutionId(1);
        final ExecutableFlow flow2 = TestUtils.createTestExecutableFlow("exectest1", "exec2");
        flow2.setExecutionId(2);
        final User testUser = TestUtils.getTestUser();
        manager.submitExecutableFlow(flow1, testUser.getUserId());
        manager.submitExecutableFlow(flow2, testUser.getUserId());
        final List<Integer> testFlows = Arrays.asList(flow1.getExecutionId(), flow2.getExecutionId());
        final List<Pair<ExecutionReference, ExecutableFlow>> queuedFlowsDB = this.loader.fetchQueuedFlows();
        Assert.assertEquals(queuedFlowsDB.size(), testFlows.size());
        // Verify things are correctly setup in db
        for (final Pair<ExecutionReference, ExecutableFlow> pair : queuedFlowsDB) {
            Assert.assertTrue(testFlows.contains(pair.getSecond().getExecutionId()));
        }
        // Verify running flows using old definition of "running" flows i.e. a
        // non-dispatched flow is also considered running
        final List<Integer> managerActiveFlows = manager.getRunningFlows().stream().map(ExecutableFlow::getExecutionId).collect(Collectors.toList());
        Assert.assertTrue(((managerActiveFlows.containsAll(testFlows)) && (testFlows.containsAll(managerActiveFlows))));
        // Verify getQueuedFlowIds method
        Assert.assertEquals("[1, 2]", manager.getQueuedFlowIds());
    }

    /* Test submit duplicate flow when previous instance is not dispatched */
    @Test(expected = ExecutorManagerException.class)
    public void testDuplicateQueuedFlows() throws Exception {
        final ExecutorManager manager = createMultiExecutorManagerInstance();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlow("exectest1", "exec1");
        flow1.getExecutionOptions().setConcurrentOption(CONCURRENT_OPTION_SKIP);
        final User testUser = TestUtils.getTestUser();
        manager.submitExecutableFlow(flow1, testUser.getUserId());
        manager.submitExecutableFlow(flow1, testUser.getUserId());
    }

    /* Test killing a job in preparation stage at webserver side i.e. a
    non-dispatched flow
     */
    @Test
    public void testKillQueuedFlow() throws Exception {
        final ExecutorManager manager = createMultiExecutorManagerInstance();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlow("exectest1", "exec1");
        final User testUser = TestUtils.getTestUser();
        manager.submitExecutableFlow(flow1, testUser.getUserId());
        manager.cancelFlow(flow1, testUser.getUserId());
        final ExecutableFlow fetchedFlow = this.loader.fetchExecutableFlow(flow1.getExecutionId());
        Assert.assertEquals(fetchedFlow.getStatus(), FAILED);
        Assert.assertFalse(manager.getRunningFlows().contains(flow1));
    }

    /* Flow has been running on an executor but is not any more (for example because of restart) */
    @Test
    public void testNotFoundFlows() throws Exception {
        testSetUpForRunningFlows();
        this.manager.start();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlow("exectest1", "exec1");
        Mockito.when(this.loader.fetchExecutableFlow((-1))).thenReturn(flow1);
        mockFlowDoesNotExist();
        this.manager.submitExecutableFlow(flow1, this.user.getUserId());
        final ExecutableFlow fetchedFlow = waitFlowFinished(flow1);
        Assert.assertEquals(fetchedFlow.getStatus(), FAILED);
    }

    /**
     * 1. Executor 1 throws an exception when trying to dispatch to it 2. ExecutorManager should try
     * next executor 3. Executor 2 accepts the dispatched execution
     */
    @Test
    public void testDispatchException() throws Exception {
        testSetUpForRunningFlows();
        this.manager.start();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlow("exectest1", "exec1");
        Mockito.doReturn(flow1).when(this.loader).fetchExecutableFlow((-1));
        mockFlowDoesNotExist();
        Mockito.when(this.apiGateway.callWithExecutable(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.eq(EXECUTE_ACTION))).thenThrow(new ExecutorManagerException("Mocked dispatch exception")).thenReturn(null);
        this.manager.submitExecutableFlow(flow1, this.user.getUserId());
        waitFlowFinished(flow1);
        Mockito.verify(this.apiGateway).callWithExecutable(flow1, this.manager.fetchExecutor(1), EXECUTE_ACTION);
        Mockito.verify(this.apiGateway).callWithExecutable(flow1, this.manager.fetchExecutor(2), EXECUTE_ACTION);
        Mockito.verify(this.loader, Mockito.times(1)).unassignExecutor((-1));
    }

    /* Added tests for runningFlows
    TODO: When removing queuedFlows cache, will refactor rest of the ExecutorManager test cases
     */
    @Test
    public void testSubmitFlows() throws Exception {
        testSetUpForRunningFlows();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlow("exectest1", "exec1");
        this.manager.submitExecutableFlow(flow1, this.user.getUserId());
        Mockito.verify(this.loader).uploadExecutableFlow(flow1);
        Mockito.verify(this.loader).addActiveExecutableReference(ArgumentMatchers.any());
    }

    // Too many concurrent flows will fail job submission
    @Test(expected = ExecutorManagerException.class)
    public void testTooManySubmitFlows() throws Exception {
        testSetUpForRunningFlows();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlowFromYaml("basicyamlshelltest", "bashSleep");
        flow1.setExecutionId(101);
        final ExecutableFlow flow2 = TestUtils.createTestExecutableFlowFromYaml("basicyamlshelltest", "bashSleep");
        flow2.setExecutionId(102);
        final ExecutableFlow flow3 = TestUtils.createTestExecutableFlowFromYaml("basicyamlshelltest", "bashSleep");
        flow3.setExecutionId(103);
        final ExecutableFlow flow4 = TestUtils.createTestExecutableFlowFromYaml("basicyamlshelltest", "bashSleep");
        flow4.setExecutionId(104);
        this.manager.submitExecutableFlow(flow1, this.user.getUserId());
        Mockito.verify(this.loader).uploadExecutableFlow(flow1);
        this.manager.submitExecutableFlow(flow2, this.user.getUserId());
        Mockito.verify(this.loader).uploadExecutableFlow(flow2);
        this.manager.submitExecutableFlow(flow3, this.user.getUserId());
        this.manager.submitExecutableFlow(flow4, this.user.getUserId());
    }

    @Test
    public void testFetchAllActiveExecutorServerHosts() throws Exception {
        testSetUpForRunningFlows();
        final Set<String> activeExecutorServerHosts = this.manager.getAllActiveExecutorServerHosts();
        final Executor executor1 = this.manager.fetchExecutor(this.flow1.getExecutionId());
        final Executor executor2 = this.manager.fetchExecutor(this.flow2.getExecutionId());
        Assert.assertTrue(activeExecutorServerHosts.contains((((executor1.getHost()) + ":") + (executor1.getPort()))));
        Assert.assertTrue(activeExecutorServerHosts.contains((((executor2.getHost()) + ":") + (executor2.getPort()))));
    }

    /**
     * ExecutorManager should try to dispatch to all executors until it succeeds.
     */
    @Test
    public void testDispatchMultipleRetries() throws Exception {
        this.props.put(MAX_DISPATCHING_ERRORS_PERMITTED, 4);
        testSetUpForRunningFlows();
        this.manager.start();
        final ExecutableFlow flow1 = TestUtils.createTestExecutableFlow("exectest1", "exec1");
        flow1.getExecutionOptions().setFailureEmails(Arrays.asList("test@example.com"));
        Mockito.when(this.loader.fetchExecutableFlow((-1))).thenReturn(flow1);
        // fail 2 first dispatch attempts, then succeed
        Mockito.when(this.apiGateway.callWithExecutable(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.eq(EXECUTE_ACTION))).thenThrow(new ExecutorManagerException("Mocked dispatch exception 1")).thenThrow(new ExecutorManagerException("Mocked dispatch exception 2")).thenReturn(null);
        // this is just to clean up the execution as FAILED after it has been submitted
        mockFlowDoesNotExist();
        this.manager.submitExecutableFlow(flow1, this.user.getUserId());
        waitFlowFinished(flow1);
        // it's random which executor is chosen each time, but both should have been tried at least once
        Mockito.verify(this.apiGateway, Mockito.atLeast(1)).callWithExecutable(flow1, this.manager.fetchExecutor(1), EXECUTE_ACTION);
        Mockito.verify(this.apiGateway, Mockito.atLeast(1)).callWithExecutable(flow1, this.manager.fetchExecutor(2), EXECUTE_ACTION);
        // verify that there was a 3rd (successful) dispatch call
        Mockito.verify(this.apiGateway, Mockito.times(3)).callWithExecutable(ArgumentMatchers.eq(flow1), ArgumentMatchers.any(), ArgumentMatchers.eq(EXECUTE_ACTION));
        Mockito.verify(this.loader, Mockito.times(2)).unassignExecutor((-1));
    }
}
