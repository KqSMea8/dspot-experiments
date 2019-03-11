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
package org.apache.activemq.karaf.itest;


import org.junit.Assert;
import org.junit.Test;


public class ActiveMQBrokerFeatureTest extends AbstractFeatureTest {
    static final String WEB_CONSOLE_URL = "http://localhost:8181/activemqweb/";

    @Test(timeout = (5 * 60) * 1000)
    public void test() throws Throwable {
        assertBrokerStarted();
        JMSTester jms = new JMSTester();
        jms.produceAndConsume(sessionFactory);
        jms.tempSendReceive();
        jms.close();
    }

    @Test
    public void testSendReceiveWeb() throws Throwable {
        assertBrokerStarted();
        JMSTester jms = new JMSTester();
        final String nameAndPayload = String.valueOf(System.currentTimeMillis());
        produceMessageWebConsole(nameAndPayload);
        Assert.assertEquals("got our message", nameAndPayload, jms.consumeMessage(nameAndPayload));
        jms.tempSendReceive();
        jms.close();
    }
}
