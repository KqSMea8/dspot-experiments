/**
 * Copyright 2018 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.conductor.core.execution.mapper;


import TaskType.HTTP;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.core.execution.ParametersUtils;
import com.netflix.conductor.core.execution.TerminateWorkflowException;
import com.netflix.conductor.core.utils.IDGenerator;
import com.netflix.conductor.dao.MetadataDAO;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class HTTPTaskMapperTest {
    private ParametersUtils parametersUtils;

    private MetadataDAO metadataDAO;

    private HTTPTaskMapper httpTaskMapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getMappedTasks() {
        // Given
        WorkflowTask taskToSchedule = new WorkflowTask();
        taskToSchedule.setName("http_task");
        taskToSchedule.setType(HTTP.name());
        taskToSchedule.setTaskDefinition(new TaskDef("http_task"));
        String taskId = IDGenerator.generate();
        String retriedTaskId = IDGenerator.generate();
        Workflow workflow = new Workflow();
        WorkflowDef workflowDef = new WorkflowDef();
        workflow.setWorkflowDefinition(workflowDef);
        TaskMapperContext taskMapperContext = TaskMapperContext.newBuilder().withWorkflowDefinition(workflowDef).withWorkflowInstance(workflow).withTaskDefinition(new TaskDef()).withTaskToSchedule(taskToSchedule).withTaskInput(new HashMap()).withRetryCount(0).withRetryTaskId(retriedTaskId).withTaskId(taskId).build();
        // when
        List<Task> mappedTasks = httpTaskMapper.getMappedTasks(taskMapperContext);
        // Then
        Assert.assertEquals(1, mappedTasks.size());
        Assert.assertEquals(HTTP.name(), mappedTasks.get(0).getTaskType());
    }

    @Test
    public void getMappedTasksException() {
        // Given
        WorkflowTask taskToSchedule = new WorkflowTask();
        taskToSchedule.setName("http_task");
        taskToSchedule.setType(HTTP.name());
        String taskId = IDGenerator.generate();
        String retriedTaskId = IDGenerator.generate();
        Workflow workflow = new Workflow();
        WorkflowDef workflowDef = new WorkflowDef();
        workflow.setWorkflowDefinition(workflowDef);
        TaskMapperContext taskMapperContext = TaskMapperContext.newBuilder().withWorkflowDefinition(workflowDef).withWorkflowInstance(workflow).withTaskToSchedule(taskToSchedule).withTaskInput(new HashMap()).withRetryCount(0).withRetryTaskId(retriedTaskId).withTaskId(taskId).build();
        // then
        expectedException.expect(TerminateWorkflowException.class);
        expectedException.expectMessage(String.format("Invalid task specified. Cannot find task by name %s in the task definitions", taskToSchedule.getName()));
        // when
        httpTaskMapper.getMappedTasks(taskMapperContext);
    }
}
