/**
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */
package alluxio.util.network;


import PropertyKey.MASTER_HOSTNAME;
import PropertyKey.MASTER_RPC_PORT;
import PropertyKey.NETWORK_HOST_RESOLUTION_TIMEOUT_MS;
import ServiceType.MASTER_RPC;
import alluxio.ConfigurationTestUtils;
import alluxio.conf.InstancedConfiguration;
import java.net.InetSocketAddress;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the {@link NetworkAddressUtils} methods related to connecting to the master and worker
 * nodes.
 */
public class GetMasterWorkerAddressTest {
    /**
     * Tests the
     * {@link NetworkAddressUtils#getConnectAddress(ServiceType, alluxio.conf.AlluxioConfiguration)}
     * method for a master node.
     */
    @Test
    public void getMasterAddress() {
        InstancedConfiguration conf = ConfigurationTestUtils.defaults();
        // connect host and port
        conf.set(MASTER_HOSTNAME, "RemoteMaster1");
        conf.set(MASTER_RPC_PORT, "10000");
        int resolveTimeout = ((int) (conf.getMs(NETWORK_HOST_RESOLUTION_TIMEOUT_MS)));
        String defaultHostname = NetworkAddressUtils.getLocalHostName(resolveTimeout);
        int defaultPort = Integer.parseInt(MASTER_RPC_PORT.getDefaultValue());
        InetSocketAddress masterAddress = NetworkAddressUtils.getConnectAddress(MASTER_RPC, conf);
        Assert.assertEquals(new InetSocketAddress("RemoteMaster1", 10000), masterAddress);
        conf = ConfigurationTestUtils.defaults();
        // port only
        conf.set(MASTER_RPC_PORT, "20000");
        masterAddress = NetworkAddressUtils.getConnectAddress(MASTER_RPC, conf);
        Assert.assertEquals(new InetSocketAddress(defaultHostname, 20000), masterAddress);
        conf = ConfigurationTestUtils.defaults();
        // connect host only
        conf.set(MASTER_HOSTNAME, "RemoteMaster3");
        masterAddress = NetworkAddressUtils.getConnectAddress(MASTER_RPC, conf);
        Assert.assertEquals(new InetSocketAddress("RemoteMaster3", defaultPort), masterAddress);
        conf = ConfigurationTestUtils.defaults();
        // all default
        masterAddress = NetworkAddressUtils.getConnectAddress(MASTER_RPC, conf);
        Assert.assertEquals(new InetSocketAddress(defaultHostname, defaultPort), masterAddress);
    }
}
