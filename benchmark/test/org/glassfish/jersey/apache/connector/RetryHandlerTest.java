/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.jersey.apache.connector;


import ApacheClientProperties.RETRY_HANDLER;
import ClientProperties.READ_TIMEOUT;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;


public class RetryHandlerTest extends JerseyTest {
    private static final int READ_TIMEOUT_MS = 100;

    @Path("/")
    public static class RetryHandlerResource {
        private static volatile int postRequestNumber = 0;

        private static volatile int getRequestNumber = 0;

        // Cause a timeout on the first GET and POST request
        @GET
        public String get(@Context
        HttpHeaders h) {
            if (((RetryHandlerTest.RetryHandlerResource.getRequestNumber)++) == 0) {
                try {
                    Thread.sleep(((RetryHandlerTest.READ_TIMEOUT_MS) * 10));
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
            return "GET";
        }

        @POST
        public String post(@Context
        HttpHeaders h, String e) {
            if (((RetryHandlerTest.RetryHandlerResource.postRequestNumber)++) == 0) {
                try {
                    Thread.sleep(((RetryHandlerTest.READ_TIMEOUT_MS) * 10));
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
            return "POST";
        }
    }

    @Test
    public void testRetryGet() throws IOException {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new ApacheConnectorProvider());
        cc.property(RETRY_HANDLER, ((HttpRequestRetryHandler) (( exception, executionCount, context) -> true)));
        cc.property(READ_TIMEOUT, RetryHandlerTest.READ_TIMEOUT_MS);
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());
        Assert.assertEquals("GET", r.request().get(String.class));
    }

    @Test
    public void testRetryPost() throws IOException {
        ClientConfig cc = new ClientConfig();
        cc.connectorProvider(new ApacheConnectorProvider());
        cc.property(RETRY_HANDLER, ((HttpRequestRetryHandler) (( exception, executionCount, context) -> true)));
        cc.property(READ_TIMEOUT, RetryHandlerTest.READ_TIMEOUT_MS);
        Client client = ClientBuilder.newClient(cc);
        WebTarget r = client.target(getBaseUri());
        Assert.assertEquals("POST", r.request().property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED).post(javax.ws.rs.client.Entity.text("POST"), String.class));
    }
}
