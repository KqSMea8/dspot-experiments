/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.test.api.event;


import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.api.delegate.event.FlowableEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;


/**
 * Test case for {@link FlowableEngineEventType#PROCESS_CREATED} event.
 *
 * @author martin.grofcik
 */
public class ProcessInstanceNameListenerTest extends PluggableFlowableTestCase {
    private ProcessInstanceNameListenerTest.TestInitializedEntityEventListener listener;

    @Test
    @Deployment(resources = { "org/flowable/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
    public void testProcessCreateProcessNameEvent() throws Exception {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder().processDefinitionKey("oneTaskProcess").name("oneTaskProcessInstanceName").start();
        assertNotNull(processInstance);
        MatcherAssert.assertThat("Process instance name must be initialized before PROCESS_CREATED event is fired.", listener.getProcessName(), CoreMatchers.is("oneTaskProcessInstanceName"));
    }

    @Test
    public void testCallActivityProcessCreatedDefinitionName() throws Exception {
        BpmnModel mainBpmnModel = loadBPMNModel("org/flowable/engine/test/bpmn/subprocess/SubProcessTest.testSuspendedProcessCallActivity_mainProcess.bpmn.xml");
        BpmnModel childBpmnModel = loadBPMNModel("org/flowable/engine/test/bpmn/subprocess/SubProcessTest.testSuspendedProcessCallActivity_childProcess.bpmn.xml");
        org.flowable.engine.repository.Deployment childDeployment = processEngine.getRepositoryService().createDeployment().name("childProcessDeployment").addBpmnModel("childProcess.bpmn20.xml", childBpmnModel).deploy();
        org.flowable.engine.repository.Deployment masterDeployment = processEngine.getRepositoryService().createDeployment().name("masterProcessDeployment").addBpmnModel("masterProcess.bpmn20.xml", mainBpmnModel).deploy();
        runtimeService.createProcessInstanceBuilder().processDefinitionKey("masterProcess").start();
        MatcherAssert.assertThat("SubProcessInstance PROCESS_CREATED event must have processDefinitionName set", listener.getProcessDefinitionName(), CoreMatchers.is("Child Process"));
        repositoryService.deleteDeployment(masterDeployment.getId(), true);
        repositoryService.deleteDeployment(childDeployment.getId());
    }

    private class TestInitializedEntityEventListener extends AbstractFlowableEngineEventListener {
        protected String processName = null;

        protected String processDefinitionName = null;

        @Override
        public void onEvent(FlowableEvent event) {
            if ((event instanceof FlowableEntityEvent) && (ProcessInstance.class.isAssignableFrom(getEntity().getClass()))) {
                // check whether entity in the event is initialized before
                // adding to the list.
                assertNotNull(getEntity());
                ProcessInstance processInstance = ((ProcessInstance) (getEntity()));
                processName = processInstance.getName();
                processDefinitionName = processInstance.getProcessDefinitionName();
            }
        }

        @Override
        public boolean isFailOnException() {
            return true;
        }

        String getProcessName() {
            return processName;
        }

        public String getProcessDefinitionName() {
            return processDefinitionName;
        }
    }
}
