package org.activiti.spring.boot.tasks;


import SpringBootTest.WebEnvironment;
import java.util.HashMap;
import java.util.Map;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class TaskVariablesLocalCopiesTest {
    private static final String TWOTASK_PROCESS = "twoTaskProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private TaskAdminRuntime taskAdminRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Test
    public void shouldGetConfiguration() {
        securityUtil.logInAs("salaboy");
        // when
        ProcessRuntimeConfiguration configuration = processRuntime.configuration();
        // then
        assertThat(configuration).isNotNull();
    }

    @Test
    public void shouldGetAvailableProcessDefinitionForTheGivenUser() {
        securityUtil.logInAs("salaboy");
        // when
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 50));
        // then
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).extracting(ProcessDefinition::getKey).contains(TaskVariablesLocalCopiesTest.TWOTASK_PROCESS);
    }

    @Test
    public void processInstanceVariablesCopiedIntoTasksByDefault() {
        securityUtil.logInAs("salaboy");
        Map<String, Object> startVariables = new HashMap<>();
        startVariables.put("start1", "start1");
        startVariables.put("start2", "start2");
        // when
        ProcessInstance twoTaskInstance = processRuntime.start(ProcessPayloadBuilder.start().withProcessDefinitionKey(TaskVariablesLocalCopiesTest.TWOTASK_PROCESS).withVariables(startVariables).build());
        assertThat(processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(twoTaskInstance).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "start1"), tuple("start2", "start2"));
        // both tasks should have the process variables
        Task task1 = taskRuntime.tasks(Pageable.of(0, 10), TaskPayloadBuilder.tasks().build()).getContent().get(0);
        assertThat(taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task1.getId()).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "start1"), tuple("start2", "start2"));
        securityUtil.logInAs("garth");
        Task task2 = taskRuntime.tasks(Pageable.of(0, 10), TaskPayloadBuilder.tasks().build()).getContent().get(0);
        assertThat(taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task2.getId()).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "start1"), tuple("start2", "start2"));
        securityUtil.logInAs("salaboy");
        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task1.getId()).build());
        // if one modifies, the other should not see the modification
        taskRuntime.updateVariable(TaskPayloadBuilder.updateVariable().withTaskId(task1.getId()).withVariable("start1", "modifiedstart1").build());
        // the task where it was modified should reflect the modification
        assertThat(taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task1.getId()).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "modifiedstart1"), tuple("start2", "start2"));
        securityUtil.logInAs("garth");
        // other does not see
        assertThat(taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task2.getId()).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "start1"), tuple("start2", "start2"));
        securityUtil.logInAs("salaboy");
        // complete and change var again
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task1.getId()).withVariable("start1", "modagainstart1").build());
        // after completion the process variable should be updated but only the one that was modified
        assertThat(processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(twoTaskInstance).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "modagainstart1"), tuple("start2", "start2"));
        securityUtil.logInAs("garth");
        // and task2 should not see the change
        assertThat(taskRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task2.getId()).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "start1"), tuple("start2", "start2"));
    }

    @Test
    public void testAdminTaskVariables() {
        securityUtil.logInAs("salaboy");
        Map<String, Object> startVariables = new HashMap<>();
        startVariables.put("start1", "start1");
        startVariables.put("start2", "start2");
        // when
        ProcessInstance twoTaskInstance = processRuntime.start(ProcessPayloadBuilder.start().withProcessDefinitionKey(TaskVariablesLocalCopiesTest.TWOTASK_PROCESS).withVariables(startVariables).build());
        assertThat(processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(twoTaskInstance).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "start1"), tuple("start2", "start2"));
        // both tasks should have the process variables
        Task task1 = taskRuntime.tasks(Pageable.of(0, 10), TaskPayloadBuilder.tasks().build()).getContent().get(0);
        // check that admin can get task variables
        securityUtil.logInAs("admin");
        assertThat(taskAdminRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task1.getId()).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "start1"), tuple("start2", "start2"));
        // check that admin can modify task variables
        taskAdminRuntime.updateVariable(TaskPayloadBuilder.updateVariable().withTaskId(task1.getId()).withVariable("start1", "modifiedstart1").build());
        // the task where it was modified should reflect the modification
        assertThat(taskAdminRuntime.variables(TaskPayloadBuilder.variables().withTaskId(task1.getId()).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "modifiedstart1"), tuple("start2", "start2"));
        securityUtil.logInAs("salaboy");
        // complete and change var again
        taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task1.getId()).build());
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task1.getId()).withVariable("start1", "modagainstart1").build());
        // after completion the process variable should be updated but only the one that was modified
        assertThat(processRuntime.variables(ProcessPayloadBuilder.variables().withProcessInstance(twoTaskInstance).build())).extracting(VariableInstance::getName, VariableInstance::getValue).containsExactly(tuple("start1", "modagainstart1"), tuple("start2", "start2"));
    }
}
