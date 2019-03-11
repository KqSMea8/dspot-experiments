/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2012 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.undertow.servlet.test.spec;


import Headers.COOKIE_STRING;
import StatusCodes.OK;
import io.undertow.testutils.DefaultServer;
import io.undertow.testutils.HttpClientUtils;
import io.undertow.testutils.TestHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests that getCookies() on a request does not fail due to invalid cookies.
 *
 * @author Gael Marziou
 */
@RunWith(DefaultServer.class)
public class GetCookiesTestCase {
    @Test
    public void testGetCookiesWithOnlyValidCookie() throws Exception {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(((DefaultServer.getDefaultServerURL()) + "/servletContext/aaa"));
            get.setHeader(COOKIE_STRING, "testcookie=works");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(OK, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            Assert.assertEquals("Only one valid cookie", "name='testcookie'value='works'", response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    @Test
    public void testGetCookiesWithOnlyInvalidCookies() throws Exception {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(((DefaultServer.getDefaultServerURL()) + "/servletContext/aaa"));
            get.setHeader(COOKIE_STRING, "ctx:123=456");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(OK, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            Assert.assertEquals("No valid cookie", "", response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    @Test
    public void testGetCookiesWithInvalidCookieName() throws Exception {
        TestHttpClient client = new TestHttpClient();
        try {
            HttpGet get = new HttpGet(((DefaultServer.getDefaultServerURL()) + "/servletContext/aaa"));
            get.setHeader(COOKIE_STRING, "testcookie=works; ctx:123=456");
            HttpResponse result = client.execute(get);
            Assert.assertEquals(OK, result.getStatusLine().getStatusCode());
            final String response = HttpClientUtils.readResponse(result);
            Assert.assertEquals("Only one valid cookie", "name='testcookie'value='works'", response);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }
}
