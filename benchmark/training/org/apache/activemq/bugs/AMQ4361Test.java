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
package org.apache.activemq.bugs;


import DeliveryMode.NON_PERSISTENT;
import Session.AUTO_ACKNOWLEDGE;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AMQ4361Test {
    private static final Logger LOG = LoggerFactory.getLogger(AMQ4361Test.class);

    private BrokerService service;

    private String brokerUrlString;

    @Test
    public void testCloseWhenHunk() throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrlString);
        connectionFactory.setProducerWindowSize(1024);
        // TINY QUEUE is flow controlled after 1024 bytes
        final ActiveMQDestination destination = ActiveMQDestination.createDestination("queue://TINY_QUEUE", ((byte) (255)));
        Connection connection = connectionFactory.createConnection();
        connection.start();
        final Session session = connection.createSession(false, AUTO_ACKNOWLEDGE);
        final MessageProducer producer = session.createProducer(destination);
        producer.setTimeToLive(0);
        producer.setDeliveryMode(NON_PERSISTENT);
        final AtomicReference<Exception> publishException = new AtomicReference<Exception>(null);
        final AtomicReference<Exception> closeException = new AtomicReference<Exception>(null);
        final AtomicLong lastLoop = new AtomicLong(((System.currentTimeMillis()) + 100));
        Thread pubThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1000];
                    new Random(-559038737).nextBytes(data);
                    for (int i = 0; i < 10000; i++) {
                        lastLoop.set(System.currentTimeMillis());
                        ObjectMessage objMsg = session.createObjectMessage();
                        objMsg.setObject(data);
                        producer.send(destination, objMsg);
                    }
                } catch (Exception e) {
                    publishException.set(e);
                }
            }
        }, "PublishingThread");
        pubThread.start();
        // wait for publisher to deadlock
        while (((System.currentTimeMillis()) - (lastLoop.get())) < 2000) {
            Thread.sleep(100);
        } 
        AMQ4361Test.LOG.info("Publisher deadlock detected.");
        Thread closeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AMQ4361Test.LOG.info("Attempting close..");
                    producer.close();
                } catch (Exception e) {
                    closeException.set(e);
                }
            }
        }, "ClosingThread");
        closeThread.start();
        try {
            closeThread.join(30000);
        } catch (InterruptedException ie) {
            Assert.assertFalse("Closing thread didn't complete in 10 seconds", true);
        }
        try {
            pubThread.join(30000);
        } catch (InterruptedException ie) {
            Assert.assertFalse("Publishing thread didn't complete in 10 seconds", true);
        }
        Assert.assertNull(closeException.get());
        Assert.assertNotNull(publishException.get());
    }
}
