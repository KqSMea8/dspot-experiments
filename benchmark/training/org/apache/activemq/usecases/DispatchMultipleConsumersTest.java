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
package org.apache.activemq.usecases;


import Session.AUTO_ACKNOWLEDGE;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *
 * @author Rajani Chennamaneni
 */
public class DispatchMultipleConsumersTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(DispatchMultipleConsumersTest.class);

    BrokerService broker;

    Destination dest;

    String destinationName = "TEST.Q";

    String msgStr = "Test text message";

    int messagesPerThread = 20;

    int producerThreads = 50;

    int consumerCount = 2;

    AtomicInteger sentCount;

    AtomicInteger consumedCount;

    CountDownLatch producerLatch;

    CountDownLatch consumerLatch;

    String brokerURL;

    String userName = "";

    String password = "";

    public void testDispatch1() {
        for (int i = 1; i <= 5; i++) {
            resetCounters();
            dispatch();
            TestCase.assertEquals(("Incorrect messages in Iteration " + i), sentCount.get(), consumedCount.get());
        }
    }

    private class ConsumerThread extends Thread {
        Session session;

        MessageConsumer consumer;

        public ConsumerThread(Connection conn, String name) {
            super();
            this.setName(name);
            DispatchMultipleConsumersTest.logger.trace(("Created new consumer thread:" + name));
            try {
                session = conn.createSession(false, AUTO_ACKNOWLEDGE);
                consumer = session.createConsumer(dest);
                start();
            } catch (JMSException e) {
                DispatchMultipleConsumersTest.logger.error(("Failed to start consumer thread:" + name), e);
            }
        }

        @Override
        public void run() {
            int msgCount = 0;
            int nullCount = 0;
            while (true) {
                try {
                    Message msg = consumer.receive(1000);
                    if (msg == null) {
                        if ((producerLatch.getCount()) > 0) {
                            continue;
                        }
                        nullCount++;
                        if (nullCount > 10) {
                            // assume that we are not getting any more messages
                            break;
                        } else {
                            continue;
                        }
                    } else {
                        nullCount = 0;
                    }
                    Thread.sleep(100);
                    if (DispatchMultipleConsumersTest.logger.isTraceEnabled()) {
                        DispatchMultipleConsumersTest.logger.trace(("Message received:" + (msg.getJMSMessageID())));
                    }
                    msgCount++;
                } catch (JMSException e) {
                    DispatchMultipleConsumersTest.logger.error("Failed to consume:", e);
                } catch (InterruptedException e) {
                    DispatchMultipleConsumersTest.logger.error("Interrupted!", e);
                }
            } 
            try {
                consumer.close();
            } catch (JMSException e) {
                DispatchMultipleConsumersTest.logger.error(("Failed to close consumer " + (getName())), e);
            }
            consumedCount.addAndGet(msgCount);
            consumerLatch.countDown();
            DispatchMultipleConsumersTest.logger.trace(((("Consumed " + msgCount) + " messages using thread ") + (getName())));
        }
    }

    private class ProducerThread extends Thread {
        int count;

        Connection conn;

        Session session;

        MessageProducer producer;

        public ProducerThread(ActiveMQConnectionFactory connFactory, int count, String name) {
            super();
            this.count = count;
            this.setName(name);
            DispatchMultipleConsumersTest.logger.trace(("Created new producer thread:" + name));
            try {
                conn = connFactory.createConnection();
                conn.start();
                session = conn.createSession(false, AUTO_ACKNOWLEDGE);
                producer = session.createProducer(dest);
                start();
            } catch (JMSException e) {
                DispatchMultipleConsumersTest.logger.error(("Failed to start producer thread:" + name), e);
            }
        }

        @Override
        public void run() {
            int i = 0;
            try {
                for (; i < (count); i++) {
                    producer.send(session.createTextMessage(msgStr));
                    Thread.sleep(500);
                }
                conn.close();
            } catch (JMSException e) {
                DispatchMultipleConsumersTest.logger.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                DispatchMultipleConsumersTest.logger.error("Interrupted!", e);
            }
            sentCount.addAndGet(i);
            producerLatch.countDown();
            if (DispatchMultipleConsumersTest.logger.isTraceEnabled()) {
                DispatchMultipleConsumersTest.logger.trace(((("Sent " + i) + " messages from thread ") + (getName())));
            }
        }
    }
}
