/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.mapred;


import MRJobConfig.TASK_EXIT_TIMEOUT;
import MRJobConfig.TASK_EXIT_TIMEOUT_CHECK_INTERVAL_MS;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.security.token.JobTokenSecretManager;
import org.apache.hadoop.mapreduce.v2.api.records.JobId;
import org.apache.hadoop.mapreduce.v2.api.records.TaskAttemptId;
import org.apache.hadoop.mapreduce.v2.api.records.TaskId;
import org.apache.hadoop.mapreduce.v2.api.records.TaskType.MAP;
import org.apache.hadoop.mapreduce.v2.app.AppContext;
import org.apache.hadoop.mapreduce.v2.app.TaskAttemptFinishingMonitor;
import org.apache.hadoop.mapreduce.v2.app.job.event.TaskAttemptEvent;
import org.apache.hadoop.mapreduce.v2.app.job.event.TaskAttemptEventType;
import org.apache.hadoop.mapreduce.v2.app.rm.RMHeartbeatHandler;
import org.apache.hadoop.mapreduce.v2.app.rm.preemption.CheckpointAMPreemptionPolicy;
import org.apache.hadoop.mapreduce.v2.util.MRBuilderUtils;
import org.apache.hadoop.yarn.event.Event;
import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.util.SystemClock;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class TestTaskAttemptFinishingMonitor {
    @Test
    public void testFinshingAttemptTimeout() throws IOException, InterruptedException {
        SystemClock clock = SystemClock.getInstance();
        Configuration conf = new Configuration();
        conf.setInt(TASK_EXIT_TIMEOUT, 100);
        conf.setInt(TASK_EXIT_TIMEOUT_CHECK_INTERVAL_MS, 10);
        AppContext appCtx = Mockito.mock(AppContext.class);
        JobTokenSecretManager secret = Mockito.mock(JobTokenSecretManager.class);
        RMHeartbeatHandler rmHeartbeatHandler = Mockito.mock(RMHeartbeatHandler.class);
        TestTaskAttemptFinishingMonitor.MockEventHandler eventHandler = new TestTaskAttemptFinishingMonitor.MockEventHandler();
        TaskAttemptFinishingMonitor taskAttemptFinishingMonitor = new TaskAttemptFinishingMonitor(eventHandler);
        taskAttemptFinishingMonitor.init(conf);
        taskAttemptFinishingMonitor.start();
        Mockito.when(appCtx.getEventHandler()).thenReturn(eventHandler);
        Mockito.when(appCtx.getNMHostname()).thenReturn("0.0.0.0");
        Mockito.when(appCtx.getTaskAttemptFinishingMonitor()).thenReturn(taskAttemptFinishingMonitor);
        Mockito.when(appCtx.getClock()).thenReturn(clock);
        CheckpointAMPreemptionPolicy policy = new CheckpointAMPreemptionPolicy();
        policy.init(appCtx);
        TaskAttemptListenerImpl listener = new TaskAttemptListenerImpl(appCtx, secret, rmHeartbeatHandler, policy);
        listener.init(conf);
        listener.start();
        JobId jid = MRBuilderUtils.newJobId(12345, 1, 1);
        TaskId tid = MRBuilderUtils.newTaskId(jid, 0, MAP);
        TaskAttemptId attemptId = MRBuilderUtils.newTaskAttemptId(tid, 0);
        appCtx.getTaskAttemptFinishingMonitor().register(attemptId);
        int check = 0;
        while ((!(eventHandler.timedOut)) && ((check++) < 10)) {
            Thread.sleep(100);
        } 
        taskAttemptFinishingMonitor.stop();
        Assert.assertTrue("Finishing attempt didn't time out.", eventHandler.timedOut);
    }

    public static class MockEventHandler implements EventHandler<Event> {
        public boolean timedOut = false;

        @Override
        public void handle(Event event) {
            if (event instanceof TaskAttemptEvent) {
                TaskAttemptEvent attemptEvent = ((TaskAttemptEvent) (event));
                if ((TaskAttemptEventType.TA_TIMED_OUT) == (attemptEvent.getType())) {
                    timedOut = true;
                }
            }
        }
    }
}
