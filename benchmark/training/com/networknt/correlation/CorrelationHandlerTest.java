/**
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.correlation;


import CorrelationHandler.config;
import Headers.HOST;
import Http2Client.BUFFER_POOL;
import Http2Client.RESPONSE_BODY;
import Http2Client.SSL;
import Http2Client.WORKER;
import HttpStringConstants.CORRELATION_ID;
import Methods.GET;
import OptionMap.EMPTY;
import com.networknt.client.Http2Client;
import com.networknt.exception.ClientException;
import io.undertow.Undertow;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;


/**
 * Created by steve on 23/09/16.
 */
public class CorrelationHandlerTest {
    static final Logger logger = LoggerFactory.getLogger(CorrelationHandlerTest.class);

    static Undertow server = null;

    @Test
    public void testWithCid() throws Exception {
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("http://localhost:8080"), WORKER, SSL, BUFFER_POOL, EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setPath("/with").setMethod(GET);
            request.getRequestHeaders().put(HOST, "localhost");
            request.getRequestHeaders().put(CORRELATION_ID, "cid");
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            CorrelationHandlerTest.logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        Assert.assertEquals("cid", body);
    }

    @Test
    public void testGetWithoutTid() throws Exception {
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("http://localhost:8080"), WORKER, SSL, BUFFER_POOL, EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setPath("/without").setMethod(GET);
            request.getRequestHeaders().put(HOST, "localhost");
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            CorrelationHandlerTest.logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        Assert.assertNotNull(body);
        System.out.println(("correlationId = " + body));
    }

    @Test
    public void testGetWithoutTidNoAutogen() throws Exception {
        // reset the autogen of the correlation ID
        config.setAutogenCorrelationID(false);
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI("http://localhost:8080"), WORKER, SSL, BUFFER_POOL, EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        try {
            ClientRequest request = new ClientRequest().setPath("/withoutNoAutogen").setMethod(GET);
            request.getRequestHeaders().put(HOST, "localhost");
            connection.sendRequest(request, client.createClientCallback(reference, latch));
            latch.await();
        } catch (Exception e) {
            CorrelationHandlerTest.logger.error("Exception: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        Assert.assertNotNull(body);
        Assert.assertEquals("noCID", body);
        System.out.println(("correlationId = " + body));
    }
}
