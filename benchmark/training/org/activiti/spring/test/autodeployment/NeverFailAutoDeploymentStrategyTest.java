package org.activiti.spring.test.autodeployment;


import org.activiti.spring.autodeployment.NeverFailAutoDeploymentStrategy;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration("classpath:org/activiti/spring/test/autodeployment/errorHandling/spring-context.xml")
public class NeverFailAutoDeploymentStrategyTest extends SpringActivitiTestCase {
    private final String nameHint = "NeverFailAutoDeploymentStrategyTest";

    private final String validName1 = "org/activiti/spring/test/autodeployment/errorHandling/valid.bpmn20.xml";

    private final String invalidName1 = "org/activiti/spring/test/autodeployment/errorHandling/parsing-error.bpmn20.xml";

    private final String invalidName2 = "org/activiti/spring/test/autodeployment/errorHandling/validation-error.bpmn20.xml";

    @Test
    public void testValidResources() {
        final Resource[] resources = new Resource[]{ new ClassPathResource(validName1) };
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testInvalidResources() {
        final Resource[] resources = new Resource[]{ new ClassPathResource(validName1), new ClassPathResource(invalidName1), new ClassPathResource(invalidName2) };
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testWithParsingErrorResources() {
        final Resource[] resources = new Resource[]{ new ClassPathResource(validName1), new ClassPathResource(invalidName1) };
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testWithValidationErrorResources() {
        final Resource[] resources = new Resource[]{ new ClassPathResource(validName1), new ClassPathResource(invalidName2) };
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(1, repositoryService.createDeploymentQuery().count());
    }

    @Test
    public void testOnlyInvalidResources() {
        final Resource[] resources = new Resource[]{ new ClassPathResource(invalidName1) };
        NeverFailAutoDeploymentStrategy deploymentStrategy = new NeverFailAutoDeploymentStrategy();
        deploymentStrategy.deployResources(nameHint, resources, repositoryService);
        assertEquals(0, repositoryService.createDeploymentQuery().count());
    }
}
