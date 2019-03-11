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
package org.apache.flink.runtime.taskmanager;


import DistributionPattern.POINTWISE;
import ResultPartitionType.PIPELINED;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.time.Deadline;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.concurrent.FutureUtils;
import org.apache.flink.runtime.execution.Environment;
import org.apache.flink.runtime.io.network.api.writer.RecordWriter;
import org.apache.flink.runtime.io.network.api.writer.ResultPartitionWriter;
import org.apache.flink.runtime.io.network.buffer.LocalBufferPoolDestroyTest;
import org.apache.flink.runtime.io.network.partition.consumer.InputGate;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobgraph.tasks.AbstractInvokable;
import org.apache.flink.runtime.jobmanager.scheduler.SlotSharingGroup;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.testingUtils.TestingUtils;
import org.apache.flink.runtime.testutils.MiniClusterResource;
import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration;
import org.apache.flink.types.LongValue;
import org.apache.flink.util.TestLogger;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import static java.lang.Thread.State.WAITING;


public class TaskCancelAsyncProducerConsumerITCase extends TestLogger {
    // The Exceptions thrown by the producer/consumer Threads
    private static volatile Exception ASYNC_PRODUCER_EXCEPTION;

    private static volatile Exception ASYNC_CONSUMER_EXCEPTION;

    // The Threads producing/consuming the intermediate stream
    private static volatile Thread ASYNC_PRODUCER_THREAD;

    private static volatile Thread ASYNC_CONSUMER_THREAD;

    @ClassRule
    public static final MiniClusterResource MINI_CLUSTER_RESOURCE = new MiniClusterResource(new MiniClusterResourceConfiguration.Builder().setConfiguration(TaskCancelAsyncProducerConsumerITCase.getFlinkConfiguration()).build());

    /**
     * Tests that a task waiting on an async producer/consumer that is stuck
     * in a blocking buffer request can be properly cancelled.
     *
     * <p>This is currently required for the Flink Kafka sources, which spawn
     * a separate Thread consuming from Kafka and producing the intermediate
     * streams in the spawned Thread instead of the main task Thread.
     */
    @Test
    public void testCancelAsyncProducerAndConsumer() throws Exception {
        Deadline deadline = Deadline.now().plus(Duration.ofMinutes(2));
        // Job with async producer and consumer
        JobVertex producer = new JobVertex("AsyncProducer");
        producer.setParallelism(1);
        producer.setInvokableClass(TaskCancelAsyncProducerConsumerITCase.AsyncProducer.class);
        JobVertex consumer = new JobVertex("AsyncConsumer");
        consumer.setParallelism(1);
        consumer.setInvokableClass(TaskCancelAsyncProducerConsumerITCase.AsyncConsumer.class);
        consumer.connectNewDataSetAsInput(producer, POINTWISE, PIPELINED);
        SlotSharingGroup slot = new SlotSharingGroup(producer.getID(), consumer.getID());
        producer.setSlotSharingGroup(slot);
        consumer.setSlotSharingGroup(slot);
        JobGraph jobGraph = new JobGraph(producer, consumer);
        final MiniCluster flink = TaskCancelAsyncProducerConsumerITCase.MINI_CLUSTER_RESOURCE.getMiniCluster();
        // Submit job and wait until running
        flink.runDetached(jobGraph);
        FutureUtils.retrySuccessfulWithDelay(() -> flink.getJobStatus(jobGraph.getJobID()), Time.milliseconds(10), deadline, ( status) -> status == JobStatus.RUNNING, TestingUtils.defaultScheduledExecutor()).get(deadline.timeLeft().toMillis(), TimeUnit.MILLISECONDS);
        boolean producerBlocked = false;
        for (int i = 0; i < 50; i++) {
            Thread thread = TaskCancelAsyncProducerConsumerITCase.ASYNC_PRODUCER_THREAD;
            if ((thread != null) && (thread.isAlive())) {
                StackTraceElement[] stackTrace = thread.getStackTrace();
                producerBlocked = LocalBufferPoolDestroyTest.isInBlockingBufferRequest(stackTrace);
            }
            if (producerBlocked) {
                break;
            } else {
                // Retry
                Thread.sleep(500L);
            }
        }
        // Verify that async producer is in blocking request
        Assert.assertTrue(("Producer thread is not blocked: " + (Arrays.toString(TaskCancelAsyncProducerConsumerITCase.ASYNC_PRODUCER_THREAD.getStackTrace()))), producerBlocked);
        boolean consumerWaiting = false;
        for (int i = 0; i < 50; i++) {
            Thread thread = TaskCancelAsyncProducerConsumerITCase.ASYNC_CONSUMER_THREAD;
            if ((thread != null) && (thread.isAlive())) {
                consumerWaiting = (thread.getState()) == (WAITING);
            }
            if (consumerWaiting) {
                break;
            } else {
                // Retry
                Thread.sleep(500L);
            }
        }
        // Verify that async consumer is in blocking request
        Assert.assertTrue("Consumer thread is not blocked.", consumerWaiting);
        flink.cancelJob(jobGraph.getJobID()).get(deadline.timeLeft().toMillis(), TimeUnit.MILLISECONDS);
        // wait until the job is canceled
        FutureUtils.retrySuccessfulWithDelay(() -> flink.getJobStatus(jobGraph.getJobID()), Time.milliseconds(10), deadline, ( status) -> status == JobStatus.CANCELED, TestingUtils.defaultScheduledExecutor()).get(deadline.timeLeft().toMillis(), TimeUnit.MILLISECONDS);
        // Verify the expected Exceptions
        Assert.assertNotNull(TaskCancelAsyncProducerConsumerITCase.ASYNC_PRODUCER_EXCEPTION);
        Assert.assertEquals(IllegalStateException.class, TaskCancelAsyncProducerConsumerITCase.ASYNC_PRODUCER_EXCEPTION.getClass());
        Assert.assertNotNull(TaskCancelAsyncProducerConsumerITCase.ASYNC_CONSUMER_EXCEPTION);
        Assert.assertEquals(IllegalStateException.class, TaskCancelAsyncProducerConsumerITCase.ASYNC_CONSUMER_EXCEPTION.getClass());
    }

    /**
     * Invokable emitting records in a separate Thread (not the main Task
     * thread).
     */
    public static class AsyncProducer extends AbstractInvokable {
        public AsyncProducer(Environment environment) {
            super(environment);
        }

        @Override
        public void invoke() throws Exception {
            Thread producer = new TaskCancelAsyncProducerConsumerITCase.AsyncProducer.ProducerThread(getEnvironment().getWriter(0));
            // Publish the async producer for the main test Thread
            TaskCancelAsyncProducerConsumerITCase.ASYNC_PRODUCER_THREAD = producer;
            producer.start();
            // Wait for the producer Thread to finish. This is executed in the
            // main Task thread and will be interrupted on cancellation.
            while (producer.isAlive()) {
                try {
                    producer.join();
                } catch (InterruptedException ignored) {
                }
            } 
        }

        /**
         * The Thread emitting the records.
         */
        private static class ProducerThread extends Thread {
            private final RecordWriter<LongValue> recordWriter;

            public ProducerThread(ResultPartitionWriter partitionWriter) {
                this.recordWriter = new RecordWriter(partitionWriter);
            }

            @Override
            public void run() {
                LongValue current = new LongValue(0);
                try {
                    while (true) {
                        current.setValue(((current.getValue()) + 1));
                        recordWriter.emit(current);
                        recordWriter.flushAll();
                    } 
                } catch (Exception e) {
                    TaskCancelAsyncProducerConsumerITCase.ASYNC_PRODUCER_EXCEPTION = e;
                }
            }
        }
    }

    /**
     * Invokable consuming buffers in a separate Thread (not the main Task
     * thread).
     */
    public static class AsyncConsumer extends AbstractInvokable {
        public AsyncConsumer(Environment environment) {
            super(environment);
        }

        @Override
        public void invoke() throws Exception {
            Thread consumer = new TaskCancelAsyncProducerConsumerITCase.AsyncConsumer.ConsumerThread(getEnvironment().getInputGate(0));
            // Publish the async consumer for the main test Thread
            TaskCancelAsyncProducerConsumerITCase.ASYNC_CONSUMER_THREAD = consumer;
            consumer.start();
            // Wait for the consumer Thread to finish. This is executed in the
            // main Task thread and will be interrupted on cancellation.
            while (consumer.isAlive()) {
                try {
                    consumer.join();
                } catch (InterruptedException ignored) {
                }
            } 
        }

        /**
         * The Thread consuming buffers.
         */
        private static class ConsumerThread extends Thread {
            private final InputGate inputGate;

            public ConsumerThread(InputGate inputGate) {
                this.inputGate = inputGate;
            }

            @Override
            public void run() {
                try {
                    while (true) {
                        inputGate.getNextBufferOrEvent();
                    } 
                } catch (Exception e) {
                    TaskCancelAsyncProducerConsumerITCase.ASYNC_CONSUMER_EXCEPTION = e;
                }
            }
        }
    }
}
