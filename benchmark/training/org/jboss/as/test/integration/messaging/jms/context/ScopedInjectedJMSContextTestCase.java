/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.integration.messaging.jms.context;


import java.util.UUID;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.test.integration.messaging.jms.context.auxiliary.BeanManagedMessageConsumer;
import org.jboss.as.test.integration.messaging.jms.context.auxiliary.RequestScopedMDB;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 *
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2013 Red Hat inc.
 */
@RunWith(Arquillian.class)
public class ScopedInjectedJMSContextTestCase {
    public static final String QUEUE_NAME_FOR_REQUEST_SCOPE = "java:global/env/ScopedInjectedJMSContextTestCaseQueue-requestScoped";

    public static final String QUEUE_NAME_FOR_TRANSACTION_SCOPE = "java:global/env/ScopedInjectedJMSContextTestCaseQueue-transactionScoped";

    @Resource(mappedName = ScopedInjectedJMSContextTestCase.QUEUE_NAME_FOR_REQUEST_SCOPE)
    private Queue queueForRequestScope;

    @Resource(mappedName = ScopedInjectedJMSContextTestCase.QUEUE_NAME_FOR_TRANSACTION_SCOPE)
    private Queue queueForTransactionScope;

    @Resource(mappedName = "/JmsXA")
    private ConnectionFactory factory;

    @EJB
    private BeanManagedMessageConsumer transactedConsumer;

    /**
     * Test that a transaction-scoped JMSContext is properly cleaned up after the transaction completion.
     */
    @Test
    public void transactionScoped() throws Exception {
        String text = UUID.randomUUID().toString();
        try (JMSContext context = factory.createContext()) {
            context.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(queueForTransactionScope, text);
            Assert.assertTrue(transactedConsumer.receive(queueForTransactionScope, text));
        }
    }

    /**
     * Test that a request-scoped JMSContext is properly cleaned up after the transaction completion.
     */
    @Test
    public void requestScoped() throws Exception {
        String text = UUID.randomUUID().toString();
        try (JMSContext context = factory.createContext()) {
            TemporaryQueue tempQueue = context.createTemporaryQueue();
            // send a request/reply message to ensure that the MDB as received the message
            // and set its consumer field
            context.createProducer().setJMSReplyTo(tempQueue).setDeliveryMode(DeliveryMode.NON_PERSISTENT).send(queueForRequestScope, text);
            JMSConsumer consumer = context.createConsumer(tempQueue);
            String reply = consumer.receiveBody(String.class, 1000);
            Assert.assertNotNull(reply);
            JMSConsumer consumerInMDB = RequestScopedMDB.consumer;
            Assert.assertNotNull(consumerInMDB);
            try {
                consumerInMDB.receiveBody(String.class);
                Assert.fail("the EJB must throw an exception as its injected JMSContext must be closed after the MDB handled the message");
            } catch (Exception e) {
            }
        }
    }
}
