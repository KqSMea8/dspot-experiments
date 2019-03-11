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
package org.jboss.as.test.integration.jca.workmanager;


import javax.annotation.Resource;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.connector.services.workmanager.NamedWorkManager;
import org.jboss.as.connector.services.workmanager.StatisticsExecutorImpl;
import org.jboss.as.test.integration.jca.JcaMgmtBase;
import org.jboss.as.test.integration.jca.JcaMgmtServerSetupTask;
import org.jboss.as.test.integration.jca.JcaTestsUtil;
import org.jboss.as.test.integration.jca.rar.MultipleAdminObject1;
import org.jboss.as.test.integration.jca.rar.MultipleAdminObject1Impl;
import org.jboss.as.test.integration.jca.rar.MultipleResourceAdapter3;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * JBQA-6454 Test case: long running threads pool and short running threads pool
 * should be different in case of using work manager with defined long-running-threads property
 * should be equal elsewhere
 *
 * @author <a href="vrastsel@redhat.com">Vladimir Rastseluev</a>
 */
@RunWith(Arquillian.class)
@ServerSetup(LongRunningThreadsCheckTestCase.TestCaseSetup.class)
public class LongRunningThreadsCheckTestCase extends JcaMgmtBase {
    public static String ctx = "customContext";

    public static String wm = "customWM";

    static class TestCaseSetup extends JcaMgmtServerSetupTask {
        private boolean enabled;

        private boolean failingOnError;

        private boolean failingOnWarning;

        @Override
        public void doSetup(final ManagementClient managementClient) throws Exception {
            enabled = getArchiveValidationAttribute("enabled");
            failingOnError = getArchiveValidationAttribute("fail-on-error");
            failingOnWarning = getArchiveValidationAttribute("fail-on-warn");
            setArchiveValidation(false, false, false);
            for (int i = 1; i <= 2; i++) {
                ModelNode wmAddress = JcaMgmtBase.subsystemAddress.clone().add("workmanager", ((LongRunningThreadsCheckTestCase.wm) + i));
                ModelNode bsAddress = JcaMgmtBase.subsystemAddress.clone().add("bootstrap-context", ((LongRunningThreadsCheckTestCase.ctx) + i));
                ModelNode operation = new ModelNode();
                try {
                    operation.get(OP).set(ADD);
                    operation.get(OP_ADDR).set(wmAddress);
                    operation.get(NAME).set(((LongRunningThreadsCheckTestCase.wm) + i));
                    executeOperation(operation);
                    operation = new ModelNode();
                    operation.get(OP).set(ADD);
                    operation.get(OP_ADDR).set(wmAddress.clone().add("short-running-threads", ((LongRunningThreadsCheckTestCase.wm) + i)));
                    operation.get("core-threads").set("20");
                    operation.get("queue-length").set("20");
                    operation.get("max-threads").set("20");
                    executeOperation(operation);
                    if (i == 2) {
                        operation = new ModelNode();
                        operation.get(OP).set(ADD);
                        operation.get(OP_ADDR).set(wmAddress.clone().add("long-running-threads", ((LongRunningThreadsCheckTestCase.wm) + i)));
                        operation.get("core-threads").set("20");
                        operation.get("queue-length").set("20");
                        operation.get("max-threads").set("20");
                        executeOperation(operation);
                    }
                    operation = new ModelNode();
                    operation.get(OP).set(ADD);
                    operation.get(OP_ADDR).set(bsAddress);
                    operation.get(NAME).set(((LongRunningThreadsCheckTestCase.ctx) + i));
                    operation.get("workmanager").set(((LongRunningThreadsCheckTestCase.wm) + i));
                    executeOperation(operation);
                } catch (Exception e) {
                    throw new Exception(((e.getMessage()) + operation), e);
                }
            }
        }
    }

    @Resource(mappedName = "java:jboss/A1")
    private MultipleAdminObject1 adminObject1;

    @Resource(mappedName = "java:jboss/A2")
    private MultipleAdminObject1 adminObject2;

    /**
     * Tests work manager without long-running-threads set
     *
     * @throws Throwable
     * 		Thrown if case of an error
     */
    @Test
    public void testWmWithoutLongRunningThreads() throws Throwable {
        Assert.assertNotNull("A1 not found", adminObject1);
        MultipleAdminObject1Impl impl = ((MultipleAdminObject1Impl) (adminObject1));
        MultipleResourceAdapter3 adapter = ((MultipleResourceAdapter3) (impl.getResourceAdapter()));
        Assert.assertNotNull(adapter);
        NamedWorkManager manager = adapter.getWorkManager();
        Assert.assertEquals(((LongRunningThreadsCheckTestCase.wm) + 1), manager.getName());
        Assert.assertTrue(((manager.getShortRunningThreadPool()) instanceof StatisticsExecutorImpl));
        Assert.assertTrue(((manager.getLongRunningThreadPool()) instanceof StatisticsExecutorImpl));
        Assert.assertEquals(JcaTestsUtil.extractBlockingExecutor(((StatisticsExecutorImpl) (manager.getShortRunningThreadPool()))), JcaTestsUtil.extractBlockingExecutor(((StatisticsExecutorImpl) (manager.getLongRunningThreadPool()))));
    }

    /**
     * Tests work manager with long-running-threads set
     *
     * @throws Throwable
     * 		Thrown if case of an error
     */
    @Test
    public void testWmWithLongRunningThreads() throws Throwable {
        Assert.assertNotNull("A2 not found", adminObject2);
        MultipleAdminObject1Impl impl = ((MultipleAdminObject1Impl) (adminObject2));
        MultipleResourceAdapter3 adapter = ((MultipleResourceAdapter3) (impl.getResourceAdapter()));
        Assert.assertNotNull(adapter);
        NamedWorkManager manager = adapter.getWorkManager();
        Assert.assertEquals(((LongRunningThreadsCheckTestCase.wm) + 2), manager.getName());
        Assert.assertFalse(manager.getShortRunningThreadPool().equals(manager.getLongRunningThreadPool()));
    }
}
