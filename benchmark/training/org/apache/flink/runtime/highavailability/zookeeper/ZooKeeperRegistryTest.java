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
package org.apache.flink.runtime.highavailability.zookeeper;


import HighAvailabilityOptions.HA_MODE;
import HighAvailabilityOptions.HA_ZOOKEEPER_QUORUM;
import JobSchedulingStatus.DONE;
import JobSchedulingStatus.PENDING;
import JobSchedulingStatus.RUNNING;
import org.apache.curator.test.TestingServer;
import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.blob.VoidBlobStore;
import org.apache.flink.runtime.concurrent.Executors;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.highavailability.RunningJobsRegistry;
import org.apache.flink.runtime.util.ZooKeeperUtils;
import org.apache.flink.util.TestLogger;
import org.junit.Assert;
import org.junit.Test;


public class ZooKeeperRegistryTest extends TestLogger {
    private TestingServer testingServer;

    /**
     * Tests that the function of ZookeeperRegistry, setJobRunning(), setJobFinished(), isJobRunning()
     */
    @Test
    public void testZooKeeperRegistry() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setString(HA_ZOOKEEPER_QUORUM, testingServer.getConnectString());
        configuration.setString(HA_MODE, "zookeeper");
        final HighAvailabilityServices zkHaService = new ZooKeeperHaServices(ZooKeeperUtils.startCuratorFramework(configuration), Executors.directExecutor(), configuration, new VoidBlobStore());
        final RunningJobsRegistry zkRegistry = zkHaService.getRunningJobsRegistry();
        try {
            JobID jobID = JobID.generate();
            Assert.assertEquals(PENDING, zkRegistry.getJobSchedulingStatus(jobID));
            zkRegistry.setJobRunning(jobID);
            Assert.assertEquals(RUNNING, zkRegistry.getJobSchedulingStatus(jobID));
            zkRegistry.setJobFinished(jobID);
            Assert.assertEquals(DONE, zkRegistry.getJobSchedulingStatus(jobID));
            zkRegistry.clearJob(jobID);
            Assert.assertEquals(PENDING, zkRegistry.getJobSchedulingStatus(jobID));
        } finally {
            zkHaService.close();
        }
    }
}
