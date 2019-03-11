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
package org.apache.zookeeper.test;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.PortAssignment;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZKTestCase;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.junit.Assert;
import org.junit.Test;


public class OOMTest extends ZKTestCase implements Watcher {
    @Test
    public void testOOM() throws IOException, InterruptedException, KeeperException {
        // This test takes too long tos run!
        if (true)
            return;

        File tmpDir = ClientBase.createTmpDir();
        // Grab some memory so that it is easier to cause an
        // OOM condition;
        List<byte[]> hog = new ArrayList<byte[]>();
        while (true) {
            try {
                hog.add(new byte[(1024 * 1024) * 2]);
            } catch (OutOfMemoryError e) {
                hog.remove(0);
                break;
            }
        } 
        ClientBase.setupTestEnv();
        ZooKeeperServer zks = new ZooKeeperServer(tmpDir, tmpDir, 3000);
        final int PORT = PortAssignment.unique();
        ServerCnxnFactory f = ServerCnxnFactory.createFactory(PORT, (-1));
        f.startup(zks);
        Assert.assertTrue("waiting for server up", ClientBase.waitForServerUp(("127.0.0.1:" + PORT), ClientBase.CONNECTION_TIMEOUT));
        System.err.println("OOM Stage 0");
        utestPrep(PORT);
        System.out.println(((((("Free = " + (Runtime.getRuntime().freeMemory())) + " total = ") + (Runtime.getRuntime().totalMemory())) + " max = ") + (Runtime.getRuntime().maxMemory())));
        System.err.println("OOM Stage 1");
        for (int i = 0; i < 1000; i++) {
            System.out.println(i);
            utestExists(PORT);
        }
        System.out.println(((((("Free = " + (Runtime.getRuntime().freeMemory())) + " total = ") + (Runtime.getRuntime().totalMemory())) + " max = ") + (Runtime.getRuntime().maxMemory())));
        System.err.println("OOM Stage 2");
        for (int i = 0; i < 1000; i++) {
            System.out.println(i);
            utestGet(PORT);
        }
        System.out.println(((((("Free = " + (Runtime.getRuntime().freeMemory())) + " total = ") + (Runtime.getRuntime().totalMemory())) + " max = ") + (Runtime.getRuntime().maxMemory())));
        System.err.println("OOM Stage 3");
        for (int i = 0; i < 1000; i++) {
            System.out.println(i);
            utestChildren(PORT);
        }
        System.out.println(((((("Free = " + (Runtime.getRuntime().freeMemory())) + " total = ") + (Runtime.getRuntime().totalMemory())) + " max = ") + (Runtime.getRuntime().maxMemory())));
        hog.get(0)[0] = ((byte) (1));
        f.shutdown();
        zks.shutdown();
        Assert.assertTrue("waiting for server down", ClientBase.waitForServerDown(("127.0.0.1:" + PORT), ClientBase.CONNECTION_TIMEOUT));
    }
}
