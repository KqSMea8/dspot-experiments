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
package org.apache.flink.runtime.jobmaster.slotpool;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.apache.flink.runtime.clusterframework.types.AllocationID;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.runtime.clusterframework.types.SlotProfile;
import org.apache.flink.runtime.executiongraph.utils.SimpleAckingTaskManagerGateway;
import org.apache.flink.runtime.instance.SlotSharingGroupId;
import org.apache.flink.runtime.jobgraph.JobVertexID;
import org.apache.flink.runtime.jobmaster.LogicalSlot;
import org.apache.flink.runtime.resourcemanager.SlotRequest;
import org.apache.flink.runtime.resourcemanager.utils.TestingResourceManagerGateway;
import org.apache.flink.runtime.taskmanager.LocalTaskManagerLocation;
import org.apache.flink.runtime.taskmanager.TaskManagerLocation;
import org.apache.flink.runtime.testingUtils.TestingUtils;
import org.apache.flink.util.ExceptionUtils;
import org.apache.flink.util.FlinkException;
import org.apache.flink.util.TestLogger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static PreviousAllocationSlotSelectionStrategy.INSTANCE;


/**
 * Test cases for slot sharing with the {@link SlotPoolImpl}.
 */
public class SlotPoolSlotSharingTest extends TestLogger {
    @Rule
    public final SlotPoolResource slotPoolResource = new SlotPoolResource(INSTANCE);

    @Test
    public void testSingleQueuedSharedSlotScheduling() throws Exception {
        final CompletableFuture<AllocationID> allocationIdFuture = new CompletableFuture<>();
        final TestingResourceManagerGateway testingResourceManagerGateway = slotPoolResource.getTestingResourceManagerGateway();
        testingResourceManagerGateway.setRequestSlotConsumer((SlotRequest slotRequest) -> allocationIdFuture.complete(slotRequest.getAllocationId()));
        LocalTaskManagerLocation taskManagerLocation = new LocalTaskManagerLocation();
        final SlotPoolImpl slotPool = slotPoolResource.getSlotPool();
        slotPool.registerTaskManager(getResourceID());
        SlotSharingGroupId slotSharingGroupId = new SlotSharingGroupId();
        final SlotProvider slotProvider = slotPoolResource.getSlotProvider();
        CompletableFuture<LogicalSlot> logicalSlotFuture = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(new JobVertexID(), slotSharingGroupId, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        Assert.assertFalse(logicalSlotFuture.isDone());
        final AllocationID allocationId = allocationIdFuture.get();
        boolean booleanCompletableFuture = slotPool.offerSlot(taskManagerLocation, new SimpleAckingTaskManagerGateway(), new org.apache.flink.runtime.taskexecutor.slot.SlotOffer(allocationId, 0, ResourceProfile.UNKNOWN));
        Assert.assertTrue(booleanCompletableFuture);
        final LogicalSlot logicalSlot = logicalSlotFuture.get();
        Assert.assertEquals(slotSharingGroupId, logicalSlot.getSlotSharingGroupId());
    }

    /**
     * Tests that returned slot futures are failed if the allocation request is failed.
     */
    @Test
    public void testFailingQueuedSharedSlotScheduling() throws Exception {
        final CompletableFuture<AllocationID> allocationIdFuture = new CompletableFuture<>();
        final TestingResourceManagerGateway testingResourceManagerGateway = slotPoolResource.getTestingResourceManagerGateway();
        testingResourceManagerGateway.setRequestSlotConsumer((SlotRequest slotRequest) -> allocationIdFuture.complete(slotRequest.getAllocationId()));
        final SlotProvider slotProvider = slotPoolResource.getSlotProvider();
        CompletableFuture<LogicalSlot> logicalSlotFuture = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(new JobVertexID(), new SlotSharingGroupId(), null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        final AllocationID allocationId = allocationIdFuture.get();
        // this should fail the returned logical slot future
        final SlotPool slotPoolGateway = slotPoolResource.getSlotPool();
        slotPoolGateway.failAllocation(allocationId, new FlinkException("Testing Exception"));
        try {
            logicalSlotFuture.get();
            Assert.fail("The slot future should have failed.");
        } catch (ExecutionException ee) {
            Assert.assertTrue(ExceptionUtils.findThrowable(ee, FlinkException.class).isPresent());
        }
    }

    /**
     * Tests queued slot scheduling with a single slot sharing group
     */
    @Test
    public void testQueuedSharedSlotScheduling() throws Exception {
        final BlockingQueue<AllocationID> allocationIds = new ArrayBlockingQueue<>(2);
        final TestingResourceManagerGateway testingResourceManagerGateway = slotPoolResource.getTestingResourceManagerGateway();
        testingResourceManagerGateway.setRequestSlotConsumer((SlotRequest slotRequest) -> allocationIds.offer(slotRequest.getAllocationId()));
        final TaskManagerLocation taskManagerLocation = new LocalTaskManagerLocation();
        final SlotPoolImpl slotPool = slotPoolResource.getSlotPool();
        slotPool.registerTaskManager(taskManagerLocation.getResourceID());
        final SlotSharingGroupId slotSharingGroupId = new SlotSharingGroupId();
        final JobVertexID jobVertexId1 = new JobVertexID();
        final JobVertexID jobVertexId2 = new JobVertexID();
        final SlotProvider slotProvider = slotPoolResource.getSlotProvider();
        CompletableFuture<LogicalSlot> logicalSlotFuture1 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId1, slotSharingGroupId, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        CompletableFuture<LogicalSlot> logicalSlotFuture2 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId2, slotSharingGroupId, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        Assert.assertFalse(logicalSlotFuture1.isDone());
        Assert.assertFalse(logicalSlotFuture2.isDone());
        final AllocationID allocationId1 = allocationIds.take();
        CompletableFuture<LogicalSlot> logicalSlotFuture3 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId1, slotSharingGroupId, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        CompletableFuture<LogicalSlot> logicalSlotFuture4 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId2, slotSharingGroupId, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        Assert.assertFalse(logicalSlotFuture3.isDone());
        Assert.assertFalse(logicalSlotFuture4.isDone());
        allocationIds.take();
        // this should fulfill the first two slot futures
        boolean offerFuture = slotPool.offerSlot(taskManagerLocation, new SimpleAckingTaskManagerGateway(), new org.apache.flink.runtime.taskexecutor.slot.SlotOffer(allocationId1, 0, ResourceProfile.UNKNOWN));
        Assert.assertTrue(offerFuture);
        LogicalSlot logicalSlot1 = logicalSlotFuture1.get();
        LogicalSlot logicalSlot2 = logicalSlotFuture2.get();
        Assert.assertEquals(logicalSlot1.getTaskManagerLocation(), logicalSlot2.getTaskManagerLocation());
        Assert.assertEquals(allocationId1, logicalSlot1.getAllocationId());
        Assert.assertEquals(allocationId1, logicalSlot2.getAllocationId());
        Assert.assertFalse(logicalSlotFuture3.isDone());
        Assert.assertFalse(logicalSlotFuture4.isDone());
        // release the shared slot by releasing the individual tasks
        logicalSlot1.releaseSlot(null);
        logicalSlot2.releaseSlot(null);
        LogicalSlot logicalSlot3 = logicalSlotFuture3.get();
        LogicalSlot logicalSlot4 = logicalSlotFuture4.get();
        Assert.assertEquals(logicalSlot3.getTaskManagerLocation(), logicalSlot4.getTaskManagerLocation());
        Assert.assertEquals(allocationId1, logicalSlot3.getAllocationId());
        Assert.assertEquals(allocationId1, logicalSlot4.getAllocationId());
    }

    /**
     * Tests queued slot scheduling with multiple slot sharing groups.
     */
    @Test
    public void testQueuedMultipleSlotSharingGroups() throws Exception {
        final BlockingQueue<AllocationID> allocationIds = new ArrayBlockingQueue<>(4);
        final TestingResourceManagerGateway testingResourceManagerGateway = slotPoolResource.getTestingResourceManagerGateway();
        testingResourceManagerGateway.setRequestSlotConsumer((SlotRequest slotRequest) -> allocationIds.offer(slotRequest.getAllocationId()));
        final TaskManagerLocation taskManagerLocation = new LocalTaskManagerLocation();
        final SlotSharingGroupId slotSharingGroupId1 = new SlotSharingGroupId();
        final SlotSharingGroupId slotSharingGroupId2 = new SlotSharingGroupId();
        final JobVertexID jobVertexId1 = new JobVertexID();
        final JobVertexID jobVertexId2 = new JobVertexID();
        final JobVertexID jobVertexId3 = new JobVertexID();
        final JobVertexID jobVertexId4 = new JobVertexID();
        final SlotPoolImpl slotPool = slotPoolResource.getSlotPool();
        slotPool.registerTaskManager(taskManagerLocation.getResourceID());
        final SlotProvider slotProvider = slotPoolResource.getSlotProvider();
        CompletableFuture<LogicalSlot> logicalSlotFuture1 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId1, slotSharingGroupId1, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        CompletableFuture<LogicalSlot> logicalSlotFuture2 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId2, slotSharingGroupId1, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        CompletableFuture<LogicalSlot> logicalSlotFuture3 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId3, slotSharingGroupId2, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        CompletableFuture<LogicalSlot> logicalSlotFuture4 = slotProvider.allocateSlot(new org.apache.flink.runtime.jobmanager.scheduler.ScheduledUnit(jobVertexId4, slotSharingGroupId2, null), true, SlotProfile.noRequirements(), TestingUtils.infiniteTime());
        Assert.assertFalse(logicalSlotFuture1.isDone());
        Assert.assertFalse(logicalSlotFuture2.isDone());
        Assert.assertFalse(logicalSlotFuture3.isDone());
        Assert.assertFalse(logicalSlotFuture4.isDone());
        // we expect two slot requests
        final AllocationID allocationId1 = allocationIds.take();
        final AllocationID allocationId2 = allocationIds.take();
        boolean offerFuture1 = slotPool.offerSlot(taskManagerLocation, new SimpleAckingTaskManagerGateway(), new org.apache.flink.runtime.taskexecutor.slot.SlotOffer(allocationId1, 0, ResourceProfile.UNKNOWN));
        boolean offerFuture2 = slotPool.offerSlot(taskManagerLocation, new SimpleAckingTaskManagerGateway(), new org.apache.flink.runtime.taskexecutor.slot.SlotOffer(allocationId2, 0, ResourceProfile.UNKNOWN));
        Assert.assertTrue(offerFuture1);
        Assert.assertTrue(offerFuture2);
        LogicalSlot logicalSlot1 = logicalSlotFuture1.get();
        LogicalSlot logicalSlot2 = logicalSlotFuture2.get();
        LogicalSlot logicalSlot3 = logicalSlotFuture3.get();
        LogicalSlot logicalSlot4 = logicalSlotFuture4.get();
        Assert.assertEquals(logicalSlot1.getTaskManagerLocation(), logicalSlot2.getTaskManagerLocation());
        Assert.assertEquals(logicalSlot3.getTaskManagerLocation(), logicalSlot4.getTaskManagerLocation());
        Assert.assertEquals(allocationId1, logicalSlot1.getAllocationId());
        Assert.assertEquals(allocationId2, logicalSlot3.getAllocationId());
    }
}
