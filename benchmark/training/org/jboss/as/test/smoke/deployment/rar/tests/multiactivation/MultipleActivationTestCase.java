/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.smoke.deployment.rar.tests.multiactivation;


import Namespace.RESOURCEADAPTERS_1_0;
import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.connector.subsystems.resourceadapters.ResourceAdapterSubsystemParser;
import org.jboss.as.test.integration.management.base.AbstractMgmtServerSetupTask;
import org.jboss.as.test.integration.management.base.ContainerResourceMgmtTestBase;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import org.jboss.as.test.shared.FileUtils;
import org.jboss.as.test.smoke.deployment.rar.MultipleAdminObject1;
import org.jboss.as.test.smoke.deployment.rar.MultipleConnectionFactory1;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 *
 *
 * @author <a href="vrastsel@redhat.com">Vladimir Rastseluev</a>
JBQA-5740 multiple resources deployment
 */
@RunWith(Arquillian.class)
@ServerSetup(MultipleActivationTestCase.MultipleActivationTestCaseSetup.class)
public class MultipleActivationTestCase extends ContainerResourceMgmtTestBase {
    static class MultipleActivationTestCaseSetup extends AbstractMgmtServerSetupTask {
        @Override
        public void doSetup(final ManagementClient managementClient) throws Exception {
            String xml = FileUtils.readFile(MultipleActivationTestCase.class, "simple.xml");
            List<ModelNode> operations = xmlToModelOperations(xml, RESOURCEADAPTERS_1_0.getUriString(), new ResourceAdapterSubsystemParser());
            executeOperation(operationListToCompositeOperation(operations));
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws IOException, MgmtOperationException {
            final ModelNode address = new ModelNode();
            address.add("subsystem", "resource-adapters");
            address.add("resource-adapter", "archive.rar");
            address.protect();
            remove(address);
        }
    }

    @Resource(mappedName = "java:jboss/name1")
    private MultipleConnectionFactory1 connectionFactory1;

    @Resource(mappedName = "java:jboss/name2")
    private MultipleConnectionFactory1 connectionFactory2;

    @Resource(mappedName = "java:jboss/Name3")
    private MultipleAdminObject1 adminObject1;

    @Resource(mappedName = "java:jboss/Name4")
    private MultipleAdminObject1 adminObject2;

    /**
     * Test configuration
     *
     * @throws Throwable
     * 		Thrown if case of an error
     */
    @Test
    public void testConfiguration() throws Throwable {
        Assert.assertNotNull("CF1 not found", connectionFactory1);
        Assert.assertNotNull("CF2 not found", connectionFactory2);
        Assert.assertNotNull("AO1 not found", adminObject1);
        Assert.assertNotNull("AO2 not found", adminObject2);
        Assert.assertEquals("not equal AOs", adminObject1, adminObject2);
    }
}
