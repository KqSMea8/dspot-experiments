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
package org.apache.zookeeper.server;


import ServerCnxnFactory.ZOOKEEPER_SERVER_CNXN_FACTORY;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.zookeeper.PortAssignment;
import org.apache.zookeeper.ZKTestCase;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.test.ClientBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class tests the startup behavior of ZooKeeper server.
 */
public class ZooKeeperServerStartupTest extends ZKTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperServerStartupTest.class);

    private static int PORT = PortAssignment.unique();

    private static String HOST = "127.0.0.1";

    private static String HOSTPORT = ((ZooKeeperServerStartupTest.HOST) + ":") + (ZooKeeperServerStartupTest.PORT);

    private ServerCnxnFactory servcnxnf;

    private ZooKeeperServer zks;

    private File tmpDir;

    private CountDownLatch startupDelayLatch = new CountDownLatch(1);

    /**
     * Test case for
     * {@link https://issues.apache.org/jira/browse/ZOOKEEPER-2383}.
     */
    @Test(timeout = 30000)
    public void testClientConnectionRequestDuringStartupWithNIOServerCnxn() throws Exception {
        tmpDir = ClientBase.createTmpDir();
        ClientBase.setupTestEnv();
        startSimpleZKServer(startupDelayLatch);
        ZooKeeperServerStartupTest.SimpleZooKeeperServer simplezks = ((ZooKeeperServerStartupTest.SimpleZooKeeperServer) (zks));
        Assert.assertTrue("Failed to invoke zks#startup() method during server startup", simplezks.waitForStartupInvocation(10));
        ClientBase.CountdownWatcher watcher = new ClientBase.CountdownWatcher();
        ZooKeeper zkClient = new ZooKeeper(ZooKeeperServerStartupTest.HOSTPORT, ClientBase.CONNECTION_TIMEOUT, watcher);
        Assert.assertFalse("Since server is not fully started, zks#createSession() shouldn't be invoked", simplezks.waitForSessionCreation(5));
        ZooKeeperServerStartupTest.LOG.info("Decrements the count of the latch, so that server will proceed with startup");
        startupDelayLatch.countDown();
        Assert.assertTrue("waiting for server being up ", ClientBase.waitForServerUp(ZooKeeperServerStartupTest.HOSTPORT, ClientBase.CONNECTION_TIMEOUT));
        Assert.assertTrue("Failed to invoke zks#createSession() method during client session creation", simplezks.waitForSessionCreation(5));
        watcher.waitForConnected(ClientBase.CONNECTION_TIMEOUT);
        zkClient.close();
    }

    /**
     * Test case for
     * {@link https://issues.apache.org/jira/browse/ZOOKEEPER-2383}.
     */
    @Test(timeout = 30000)
    public void testClientConnectionRequestDuringStartupWithNettyServerCnxn() throws Exception {
        tmpDir = ClientBase.createTmpDir();
        ClientBase.setupTestEnv();
        String originalServerCnxnFactory = System.getProperty(ZOOKEEPER_SERVER_CNXN_FACTORY);
        try {
            System.setProperty(ZOOKEEPER_SERVER_CNXN_FACTORY, NettyServerCnxnFactory.class.getName());
            startSimpleZKServer(startupDelayLatch);
            ZooKeeperServerStartupTest.SimpleZooKeeperServer simplezks = ((ZooKeeperServerStartupTest.SimpleZooKeeperServer) (zks));
            Assert.assertTrue("Failed to invoke zks#startup() method during server startup", simplezks.waitForStartupInvocation(10));
            ClientBase.CountdownWatcher watcher = new ClientBase.CountdownWatcher();
            ZooKeeper zkClient = new ZooKeeper(ZooKeeperServerStartupTest.HOSTPORT, ClientBase.CONNECTION_TIMEOUT, watcher);
            Assert.assertFalse("Since server is not fully started, zks#createSession() shouldn't be invoked", simplezks.waitForSessionCreation(5));
            ZooKeeperServerStartupTest.LOG.info("Decrements the count of the latch, so that server will proceed with startup");
            startupDelayLatch.countDown();
            Assert.assertTrue("waiting for server being up ", ClientBase.waitForServerUp(ZooKeeperServerStartupTest.HOSTPORT, ClientBase.CONNECTION_TIMEOUT));
            Assert.assertTrue("Failed to invoke zks#createSession() method during client session creation", simplezks.waitForSessionCreation(5));
            watcher.waitForConnected(ClientBase.CONNECTION_TIMEOUT);
            zkClient.close();
        } finally {
            // reset cnxn factory
            if (originalServerCnxnFactory == null) {
                System.clearProperty(ZOOKEEPER_SERVER_CNXN_FACTORY);
                return;
            }
            System.setProperty(ZOOKEEPER_SERVER_CNXN_FACTORY, originalServerCnxnFactory);
        }
    }

    /**
     * Test case for
     * {@link https://issues.apache.org/jira/browse/ZOOKEEPER-2383}.
     */
    @Test(timeout = 30000)
    public void testFourLetterWords() throws Exception {
        startSimpleZKServer(startupDelayLatch);
        verify("conf", ZK_NOT_SERVING);
        verify("crst", ZK_NOT_SERVING);
        verify("cons", ZK_NOT_SERVING);
        verify("dirs", ZK_NOT_SERVING);
        verify("dump", ZK_NOT_SERVING);
        verify("mntr", ZK_NOT_SERVING);
        verify("stat", ZK_NOT_SERVING);
        verify("srst", ZK_NOT_SERVING);
        verify("wchp", ZK_NOT_SERVING);
        verify("wchc", ZK_NOT_SERVING);
        verify("wchs", ZK_NOT_SERVING);
        verify("isro", "null");
    }

    private static class SimpleZooKeeperServer extends ZooKeeperServer {
        private CountDownLatch startupDelayLatch;

        private CountDownLatch startupInvokedLatch = new CountDownLatch(1);

        private CountDownLatch createSessionInvokedLatch = new CountDownLatch(1);

        public SimpleZooKeeperServer(File snapDir, File logDir, int tickTime, CountDownLatch startupDelayLatch) throws IOException {
            super(snapDir, logDir, tickTime);
            this.startupDelayLatch = startupDelayLatch;
        }

        @Override
        public synchronized void startup() {
            try {
                startupInvokedLatch.countDown();
                // Delaying the zk server startup so that
                // ZooKeeperServer#sessionTracker reference won't be
                // initialized. In the defect scenario, while processing the
                // connection request zkServer needs sessionTracker reference,
                // but this is not yet initialized and the server is still in
                // the startup phase, resulting in NPE.
                startupDelayLatch.await();
            } catch (InterruptedException e) {
                Assert.fail("Unexpected InterruptedException while startinng up!");
            }
            super.startup();
        }

        @Override
        long createSession(ServerCnxn cnxn, byte[] passwd, int timeout) {
            createSessionInvokedLatch.countDown();
            return super.createSession(cnxn, passwd, timeout);
        }

        boolean waitForStartupInvocation(long timeout) throws InterruptedException {
            return startupInvokedLatch.await(timeout, TimeUnit.SECONDS);
        }

        boolean waitForSessionCreation(long timeout) throws InterruptedException {
            return createSessionInvokedLatch.await(timeout, TimeUnit.SECONDS);
        }
    }
}
