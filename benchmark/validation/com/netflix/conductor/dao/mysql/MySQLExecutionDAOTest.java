/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.conductor.dao.mysql;


import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.dao.ExecutionDAOTest;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;


@SuppressWarnings("Duplicates")
public class MySQLExecutionDAOTest extends ExecutionDAOTest {
    private MySQLDAOTestUtil testMySQL;

    private MySQLExecutionDAO executionDAO;

    @Rule
    public TestName name = new TestName();

    @Test
    public void testPendingByCorrelationId() {
        WorkflowDef def = new WorkflowDef();
        def.setName("pending_count_correlation_jtest");
        Workflow workflow = createTestWorkflow();
        workflow.setWorkflowDefinition(def);
        generateWorkflows(workflow, 10);
        List<Workflow> bycorrelationId = getExecutionDAO().getWorkflowsByCorrelationId("corr001", true);
        Assert.assertNotNull(bycorrelationId);
        Assert.assertEquals(10, bycorrelationId.size());
    }
}
