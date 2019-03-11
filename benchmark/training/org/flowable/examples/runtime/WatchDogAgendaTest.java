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
package org.flowable.examples.runtime;


import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.impl.test.ResourceFlowableTestCase;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.Task;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


/**
 * This class shows an example of configurable agenda usage.
 */
public class WatchDogAgendaTest extends ResourceFlowableTestCase {
    public WatchDogAgendaTest() {
        super("org/flowable/examples/runtime/WatchDogAgendaTest.flowable.cfg.xml");
    }

    @Test
    @Deployment(resources = "org/flowable/engine/test/api/oneTaskProcess.bpmn20.xml")
    public void testWatchDogWithOneTaskProcess() {
        this.runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Task task = this.taskService.createTaskQuery().singleResult();
        this.taskService.complete(task.getId());
        Assert.assertThat(this.runtimeService.createProcessInstanceQuery().count(), CoreMatchers.is(0L));
    }

    @Test
    @Deployment(resources = "org/flowable/examples/runtime/WatchDogAgendaTest-endlessloop.bpmn20.xml")
    public void testWatchDogWithEndLessLoop() {
        try {
            this.runtimeService.startProcessInstanceByKey("endlessloop");
            fail("ActivitiException with 'WatchDog limit exceeded.' message expected.");
        } catch (FlowableException e) {
            if (!("WatchDog limit exceeded.".equals(e.getMessage()))) {
                fail(("Unexpected exception " + e));
            }
        }
    }
}
