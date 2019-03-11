/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.spi.discovery.zk.internal;


import CreateMode.PERSISTENT;
import ZooDefs.Ids.OPEN_ACL_UNSAFE;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.util.future.GridFutureAdapter;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.spi.discovery.tcp.ipfinder.zk.curator.TestingCluster;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;


/**
 *
 */
public class ZookeeperClientTest extends GridCommonAbstractTest {
    /**
     *
     */
    private static final int SES_TIMEOUT = 60000;

    /**
     *
     */
    private TestingCluster zkCluster;

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testSaveLargeValue() throws Exception {
        startZK(1);
        final ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        byte[] data = new byte[1024 * 1024];
        String basePath = "/ignite";
        assertTrue(client.needSplitNodeData(basePath, data, 2));
        List<byte[]> parts = client.splitNodeData(basePath, data, 2);
        assertTrue(((parts.size()) > 1));
        ZooKeeper zk = client.zk();
        for (int i = 0; i < (parts.size()); i++) {
            byte[] part = parts.get(i);
            assertTrue(((part.length) > 0));
            String path0 = (basePath + ":") + i;
            zk.create(path0, part, OPEN_ACL_UNSAFE, PERSISTENT);
        }
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testClose() throws Exception {
        startZK(1);
        final ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        client.zk().close();
        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                client.createIfNeeded("/apacheIgnite2", null, PERSISTENT);
                return null;
            }
        }, ZookeeperClientFailedException.class, null);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCreateAll() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite", null, PERSISTENT);
        List<String> paths = new ArrayList<>();
        paths.add("/apacheIgnite/1");
        paths.add("/apacheIgnite/2");
        paths.add("/apacheIgnite/3");
        client.createAll(paths, PERSISTENT);
        assertEquals(3, client.getChildren("/apacheIgnite").size());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCreateAllRequestOverflow() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite", null, PERSISTENT);
        int cnt = 20000;
        List<String> paths = new ArrayList<>(cnt);
        for (int i = 0; i < cnt; i++)
            paths.add(("/apacheIgnite/" + i));

        client.createAll(paths, PERSISTENT);
        assertEquals(cnt, client.getChildren("/apacheIgnite").size());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCreateAllNodeExists() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite", null, PERSISTENT);
        client.createIfNeeded("/apacheIgnite/1", null, PERSISTENT);
        List<String> paths = new ArrayList<>();
        paths.add("/apacheIgnite/1");
        paths.add("/apacheIgnite/2");
        paths.add("/apacheIgnite/3");
        client.createAll(paths, PERSISTENT);
        assertEquals(3, client.getChildren("/apacheIgnite").size());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDeleteAll() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite", null, PERSISTENT);
        client.createIfNeeded("/apacheIgnite/1", null, PERSISTENT);
        client.createIfNeeded("/apacheIgnite/2", null, PERSISTENT);
        client.deleteAll(Arrays.asList("/apacheIgnite/1", "/apacheIgnite/2"), (-1));
        assertTrue(client.getChildren("/apacheIgnite").isEmpty());
        client.createIfNeeded("/apacheIgnite/1", null, PERSISTENT);
        client.deleteAll(Collections.singletonList("/apacheIgnite/1"), (-1));
        assertTrue(client.getChildren("/apacheIgnite").isEmpty());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDeleteAllRequestOverflow() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite", null, PERSISTENT);
        int cnt = 30000;
        List<String> paths = new ArrayList<>(cnt);
        for (int i = 0; i < cnt; i++)
            paths.add(("/apacheIgnite/" + i));

        client.createAll(paths, PERSISTENT);
        assertEquals(cnt, client.getChildren("/apacheIgnite").size());
        client.deleteAll(paths, (-1));
        assertTrue(client.getChildren("/apacheIgnite").isEmpty());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDeleteAllNoNode() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite", null, PERSISTENT);
        client.createIfNeeded("/apacheIgnite/1", null, PERSISTENT);
        client.createIfNeeded("/apacheIgnite/2", null, PERSISTENT);
        client.deleteAll(Arrays.asList("/apacheIgnite/1", "/apacheIgnite/2", "/apacheIgnite/3"), (-1));
        assertTrue(client.getChildren("/apacheIgnite").isEmpty());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testConnectionLoss1() throws Exception {
        ZookeeperClient client = new ZookeeperClient(log, "localhost:2200", 3000, null);
        try {
            client.createIfNeeded("/apacheIgnite", null, PERSISTENT);
            fail();
        } catch (ZookeeperClientFailedException e) {
            info(("Expected error: " + e));
        }
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testConnectionLoss2() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(3000);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        closeZK();
        try {
            client.createIfNeeded("/apacheIgnite2", null, PERSISTENT);
            fail();
        } catch (ZookeeperClientFailedException e) {
            info(("Expected error: " + e));
        }
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testConnectionLoss3() throws Exception {
        startZK(1);
        ZookeeperClientTest.CallbackFuture cb = new ZookeeperClientTest.CallbackFuture();
        ZookeeperClient client = new ZookeeperClient(log, zkCluster.getConnectString(), 3000, cb);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        closeZK();
        final AtomicBoolean res = new AtomicBoolean();
        client.getChildrenAsync("/apacheIgnite1", null, new AsyncCallback.Children2Callback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
                if (rc == 0)
                    res.set(true);

            }
        });
        cb.get(60000);
        assertFalse(res.get());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testConnectionLoss4() throws Exception {
        startZK(1);
        ZookeeperClientTest.CallbackFuture cb = new ZookeeperClientTest.CallbackFuture();
        final ZookeeperClient client = new ZookeeperClient(log, zkCluster.getConnectString(), 3000, cb);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        final CountDownLatch l = new CountDownLatch(1);
        client.getChildrenAsync("/apacheIgnite1", null, new AsyncCallback.Children2Callback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
                closeZK();
                try {
                    client.createIfNeeded("/apacheIgnite2", null, PERSISTENT);
                } catch (ZookeeperClientFailedException e) {
                    info(("Expected error: " + e));
                    l.countDown();
                } catch (Exception e) {
                    fail(("Unexpected error: " + e));
                }
            }
        });
        assertTrue(l.await(10, TimeUnit.SECONDS));
        get();
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReconnect1() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        zkCluster.getServers().get(0).stop();
        IgniteInternalFuture fut = GridTestUtils.runAsync(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                U.sleep(2000);
                info("Restart zookeeper server");
                zkCluster.getServers().get(0).restart();
                info("Zookeeper server restarted");
                return null;
            }
        }, "start-zk");
        client.createIfNeeded("/apacheIgnite2", null, PERSISTENT);
        fut.get();
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReconnect1_Callback() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        zkCluster.getServers().get(0).stop();
        final CountDownLatch l = new CountDownLatch(1);
        client.getChildrenAsync("/apacheIgnite1", null, new AsyncCallback.Children2Callback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
                info(("Callback: " + rc));
                if (rc == 0)
                    l.countDown();

            }
        });
        IgniteInternalFuture fut = GridTestUtils.runAsync(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                U.sleep(2000);
                info("Restart zookeeper server");
                zkCluster.getServers().get(0).restart();
                info("Zookeeper server restarted");
                return null;
            }
        }, "start-zk");
        assertTrue(l.await(10, TimeUnit.SECONDS));
        fut.get();
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReconnect1_InCallback() throws Exception {
        startZK(1);
        final ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        final CountDownLatch l = new CountDownLatch(1);
        client.getChildrenAsync("/apacheIgnite1", null, new AsyncCallback.Children2Callback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
                try {
                    zkCluster.getServers().get(0).stop();
                    IgniteInternalFuture fut = GridTestUtils.runAsync(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            U.sleep(2000);
                            info("Restart zookeeper server");
                            zkCluster.getServers().get(0).restart();
                            info("Zookeeper server restarted");
                            return null;
                        }
                    }, "start-zk");
                    client.createIfNeeded("/apacheIgnite2", null, PERSISTENT);
                    l.countDown();
                    fut.get();
                } catch (Exception e) {
                    fail(("Unexpected error: " + e));
                }
            }
        });
        assertTrue(l.await(10, TimeUnit.SECONDS));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReconnect2() throws Exception {
        startZK(1);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        zkCluster.getServers().get(0).restart();
        client.createIfNeeded("/apacheIgnite2", null, PERSISTENT);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReconnect3() throws Exception {
        startZK(3);
        ZookeeperClient client = createClient(ZookeeperClientTest.SES_TIMEOUT);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < 30; i++) {
            info(("Iteration: " + i));
            int idx = rnd.nextInt(3);
            zkCluster.getServers().get(idx).restart();
            doSleep(((rnd.nextLong(100)) + 1));
            client.createIfNeeded(("/apacheIgnite" + i), null, PERSISTENT);
        }
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReconnect4() throws Exception {
        startZK(3);
        ZookeeperClient client = new ZookeeperClient(log, zkCluster.getServers().get(2).getInstanceSpec().getConnectString(), 60000, null);
        client.createIfNeeded("/apacheIgnite1", null, PERSISTENT);
        zkCluster.getServers().get(0).stop();
        zkCluster.getServers().get(1).stop();
        IgniteInternalFuture fut = GridTestUtils.runAsync(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                U.sleep(2000);
                info("Restart zookeeper server");
                zkCluster.getServers().get(0).restart();
                info("Zookeeper server restarted");
                return null;
            }
        }, "start-zk");
        client.createIfNeeded("/apacheIgnite2", null, PERSISTENT);
        fut.get();
    }

    /**
     *
     */
    private static class CallbackFuture extends GridFutureAdapter<Void> implements IgniteRunnable {
        /**
         * {@inheritDoc }
         */
        @Override
        public void run() {
            onDone();
        }
    }
}
