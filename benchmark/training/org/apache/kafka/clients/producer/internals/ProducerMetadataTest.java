/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.clients.producer.internals;


import Time.SYSTEM;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.internals.ClusterResourceListeners;
import org.apache.kafka.common.utils.LogContext;
import org.apache.kafka.common.utils.Time;
import org.junit.Assert;
import org.junit.Test;

import static ProducerMetadata.TOPIC_EXPIRY_MS;


public class ProducerMetadataTest {
    private long refreshBackoffMs = 100;

    private long metadataExpireMs = 1000;

    private ProducerMetadata metadata = new ProducerMetadata(refreshBackoffMs, metadataExpireMs, new LogContext(), new ClusterResourceListeners(), Time.SYSTEM);

    private AtomicReference<Exception> backgroundError = new AtomicReference<>();

    @Test
    public void testMetadata() throws Exception {
        String topic = "my-topic";
        metadata.add(topic);
        long time = SYSTEM.milliseconds();
        metadata.update(responseWithTopics(Collections.emptySet()), time);
        Assert.assertTrue("No update needed.", ((metadata.timeToNextUpdate(time)) > 0));
        metadata.requestUpdate();
        Assert.assertTrue("Still no updated needed due to backoff", ((metadata.timeToNextUpdate(time)) > 0));
        time += refreshBackoffMs;
        Assert.assertEquals("Update needed now that backoff time expired", 0, metadata.timeToNextUpdate(time));
        Thread t1 = asyncFetch(topic, 500);
        Thread t2 = asyncFetch(topic, 500);
        Assert.assertTrue("Awaiting update", t1.isAlive());
        Assert.assertTrue("Awaiting update", t2.isAlive());
        // Perform metadata update when an update is requested on the async fetch thread
        // This simulates the metadata update sequence in KafkaProducer
        while ((t1.isAlive()) || (t2.isAlive())) {
            if ((metadata.timeToNextUpdate(time)) == 0) {
                metadata.update(responseWithCurrentTopics(), time);
                time += refreshBackoffMs;
            }
            Thread.sleep(1);
        } 
        t1.join();
        t2.join();
        Assert.assertTrue("No update needed.", ((metadata.timeToNextUpdate(time)) > 0));
        time += metadataExpireMs;
        Assert.assertEquals("Update needed due to stale metadata.", 0, metadata.timeToNextUpdate(time));
    }

    @Test
    public void testMetadataAwaitAfterClose() throws InterruptedException {
        long time = 0;
        metadata.update(responseWithCurrentTopics(), time);
        Assert.assertTrue("No update needed.", ((metadata.timeToNextUpdate(time)) > 0));
        metadata.requestUpdate();
        Assert.assertTrue("Still no updated needed due to backoff", ((metadata.timeToNextUpdate(time)) > 0));
        time += refreshBackoffMs;
        Assert.assertEquals("Update needed now that backoff time expired", 0, metadata.timeToNextUpdate(time));
        String topic = "my-topic";
        metadata.close();
        Thread t1 = asyncFetch(topic, 500);
        t1.join();
        Assert.assertEquals(KafkaException.class, backgroundError.get().getClass());
        Assert.assertTrue(backgroundError.get().toString().contains("Requested metadata update after close"));
        clearBackgroundError();
    }

    /**
     * Tests that {@link org.apache.kafka.clients.producer.internals.ProducerMetadata#awaitUpdate(int, long)} doesn't
     * wait forever with a max timeout value of 0
     *
     * @throws Exception
     * 		
     * @see <a href=https://issues.apache.org/jira/browse/KAFKA-1836>KAFKA-1836</a>
     */
    @Test
    public void testMetadataUpdateWaitTime() throws Exception {
        long time = 0;
        metadata.update(responseWithCurrentTopics(), time);
        Assert.assertTrue("No update needed.", ((metadata.timeToNextUpdate(time)) > 0));
        // first try with a max wait time of 0 and ensure that this returns back without waiting forever
        try {
            metadata.awaitUpdate(metadata.requestUpdate(), 0);
            Assert.fail("Wait on metadata update was expected to timeout, but it didn't");
        } catch (TimeoutException te) {
            // expected
        }
        // now try with a higher timeout value once
        final long twoSecondWait = 2000;
        try {
            metadata.awaitUpdate(metadata.requestUpdate(), twoSecondWait);
            Assert.fail("Wait on metadata update was expected to timeout, but it didn't");
        } catch (TimeoutException te) {
            // expected
        }
    }

    @Test
    public void testTimeToNextUpdateOverwriteBackoff() {
        long now = 10000;
        // New topic added to fetch set and update requested. It should allow immediate update.
        metadata.update(responseWithCurrentTopics(), now);
        metadata.add("new-topic");
        Assert.assertEquals(0, metadata.timeToNextUpdate(now));
        // Even though add is called, immediate update isn't necessary if the new topic set isn't
        // containing a new topic,
        metadata.update(responseWithCurrentTopics(), now);
        metadata.add("new-topic");
        Assert.assertEquals(metadataExpireMs, metadata.timeToNextUpdate(now));
        // If the new set of topics containing a new topic then it should allow immediate update.
        metadata.add("another-new-topic");
        Assert.assertEquals(0, metadata.timeToNextUpdate(now));
    }

    @Test
    public void testTopicExpiry() {
        // Test that topic is expired if not used within the expiry interval
        long time = 0;
        String topic1 = "topic1";
        metadata.add(topic1);
        metadata.update(responseWithCurrentTopics(), time);
        Assert.assertTrue(metadata.containsTopic(topic1));
        time += TOPIC_EXPIRY_MS;
        metadata.update(responseWithCurrentTopics(), time);
        Assert.assertFalse("Unused topic not expired", metadata.containsTopic(topic1));
        // Test that topic is not expired if used within the expiry interval
        metadata.add("topic2");
        metadata.update(responseWithCurrentTopics(), time);
        for (int i = 0; i < 3; i++) {
            time += (TOPIC_EXPIRY_MS) / 2;
            metadata.update(responseWithCurrentTopics(), time);
            Assert.assertTrue("Topic expired even though in use", metadata.containsTopic("topic2"));
            metadata.add("topic2");
        }
    }
}
