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
package org.apache.activemq.security;


import junit.framework.TestCase;
import org.apache.activemq.broker.BrokerService;
import org.junit.Test;


public class CRLTest {
    BrokerService broker;

    @Test
    public void testCRL() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "src/test/resources/org/apache/activemq/security/client.ts");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStoreType", "jks");
        System.setProperty("javax.net.ssl.keyStore", "src/test/resources/org/apache/activemq/security/activemq-revoke.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.keyStoreType", "jks");
        boolean failed = false;
        try {
            basicSendReceive("ssl://localhost:61617");
        } catch (Exception e) {
            failed = true;
        }
        TestCase.assertTrue("Send should have failed", failed);
    }
}
