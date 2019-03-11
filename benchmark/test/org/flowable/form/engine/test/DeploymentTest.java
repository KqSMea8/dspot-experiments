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
package org.flowable.form.engine.test;


import java.util.List;
import org.flowable.form.api.FormDefinition;
import org.flowable.form.api.FormDeployment;
import org.flowable.form.api.FormInfo;
import org.flowable.form.model.SimpleFormModel;
import org.junit.Assert;
import org.junit.jupiter.api.Test;


public class DeploymentTest extends AbstractFlowableFormTest {
    @Test
    @FormDeploymentAnnotation(resources = "org/flowable/form/engine/test/deployment/simple.form")
    public void deploySingleForm() {
        FormDefinition formDefinition = repositoryService.createFormDefinitionQuery().latestVersion().formDefinitionKey("form1").singleResult();
        Assert.assertNotNull(formDefinition);
        Assert.assertEquals("form1", formDefinition.getKey());
    }

    @Test
    @FormDeploymentAnnotation(resources = "org/flowable/form/engine/test/deployment/simple.form")
    public void redeploySingleForm() {
        FormDefinition formDefinition = repositoryService.createFormDefinitionQuery().latestVersion().formDefinitionKey("form1").singleResult();
        Assert.assertNotNull(formDefinition);
        Assert.assertEquals("form1", formDefinition.getKey());
        Assert.assertEquals(1, formDefinition.getVersion());
        FormInfo formInfo = repositoryService.getFormModelByKey("form1");
        SimpleFormModel formModel = ((SimpleFormModel) (formInfo.getFormModel()));
        Assert.assertEquals(1, formModel.getFields().size());
        Assert.assertEquals("input1", formModel.getFields().get(0).getId());
        Assert.assertEquals("Input1", formModel.getFields().get(0).getName());
        FormDeployment redeployment = repositoryService.createDeployment().addClasspathResource("org/flowable/form/engine/test/deployment/simple2.form").deploy();
        formDefinition = repositoryService.createFormDefinitionQuery().latestVersion().formDefinitionKey("form1").singleResult();
        Assert.assertNotNull(formDefinition);
        Assert.assertEquals("form1", formDefinition.getKey());
        Assert.assertEquals(2, formDefinition.getVersion());
        formInfo = repositoryService.getFormModelByKey("form1");
        formModel = ((SimpleFormModel) (formInfo.getFormModel()));
        Assert.assertEquals(1, formModel.getFields().size());
        Assert.assertEquals("input2", formModel.getFields().get(0).getId());
        Assert.assertEquals("Input2", formModel.getFields().get(0).getName());
        repositoryService.deleteDeployment(redeployment.getId());
    }

    @Test
    @FormDeploymentAnnotation(resources = { "org/flowable/form/engine/test/deployment/simple.form", "org/flowable/form/engine/test/deployment/form_with_dates.form" })
    public void deploy2Forms() {
        List<FormDefinition> formDefinitions = repositoryService.createFormDefinitionQuery().orderByFormName().asc().list();
        Assert.assertEquals(2, formDefinitions.size());
        Assert.assertEquals("My date form", formDefinitions.get(0).getName());
        Assert.assertEquals("My first form", formDefinitions.get(1).getName());
    }

    @Test
    public void deploySingleFormWithParentDeploymentId() {
        FormDeployment deployment = repositoryService.createDeployment().addClasspathResource("org/flowable/form/engine/test/deployment/simple.form").parentDeploymentId("someDeploymentId").deploy();
        FormDeployment newDeployment = repositoryService.createDeployment().addClasspathResource("org/flowable/form/engine/test/deployment/simple.form").deploy();
        try {
            FormDefinition definition = repositoryService.createFormDefinitionQuery().deploymentId(deployment.getId()).singleResult();
            Assert.assertNotNull(definition);
            Assert.assertEquals("form1", definition.getKey());
            Assert.assertEquals(1, definition.getVersion());
            FormDefinition newDefinition = repositoryService.createFormDefinitionQuery().deploymentId(newDeployment.getId()).singleResult();
            Assert.assertNotNull(newDefinition);
            Assert.assertEquals("form1", newDefinition.getKey());
            Assert.assertEquals(2, newDefinition.getVersion());
            FormInfo formInfo = repositoryService.getFormModelByKeyAndParentDeploymentId("form1", "someDeploymentId");
            Assert.assertEquals("form1", formInfo.getKey());
            Assert.assertEquals(1, formInfo.getVersion());
            formEngineConfiguration.setAlwaysLookupLatestDefinitionVersion(true);
            formInfo = repositoryService.getFormModelByKeyAndParentDeploymentId("form1", "someDeploymentId");
            Assert.assertEquals("form1", formInfo.getKey());
            Assert.assertEquals(2, formInfo.getVersion());
        } finally {
            formEngineConfiguration.setAlwaysLookupLatestDefinitionVersion(false);
            repositoryService.deleteDeployment(deployment.getId());
            repositoryService.deleteDeployment(newDeployment.getId());
        }
    }
}
