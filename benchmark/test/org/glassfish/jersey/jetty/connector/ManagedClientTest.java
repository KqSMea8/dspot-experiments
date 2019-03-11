/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.jetty.connector;


import MediaType.TEXT_PLAIN;
import Response.Status.FORBIDDEN;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ClientBinding;
import org.glassfish.jersey.server.Uri;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;


/**
 * Jersey programmatic managed client test
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ManagedClientTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(ManagedClientTest.class.getName());

    /**
     * Managed client configuration for client A.
     */
    @ClientBinding(configClass = ManagedClientTest.MyClientAConfig.class)
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    public static @interface ClientA {}

    /**
     * Managed client configuration for client B.
     */
    @ClientBinding(configClass = ManagedClientTest.MyClientBConfig.class)
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER })
    public @interface ClientB {}

    /**
     * Dynamic feature that appends a properly configured {@link CustomHeaderFilter} instance
     * to every method that is annotated with {@link Require &#64;Require} internal feature
     * annotation.
     */
    public static class CustomHeaderFeature implements DynamicFeature {
        /**
         * A method annotation to be placed on those resource methods to which a validating
         * {@link CustomHeaderFilter} instance should be added.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @Documented
        @Target(ElementType.METHOD)
        public static @interface Require {
            /**
             * Expected custom header name to be validated by the {@link CustomHeaderFilter}.
             */
            String headerName();

            /**
             * Expected custom header value to be validated by the {@link CustomHeaderFilter}.
             */
            String headerValue();
        }

        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            final ManagedClientTest.CustomHeaderFeature.Require va = resourceInfo.getResourceMethod().getAnnotation(ManagedClientTest.CustomHeaderFeature.Require.class);
            if (va != null) {
                context.register(new ManagedClientTest.CustomHeaderFilter(va.headerName(), va.headerValue()));
            }
        }
    }

    /**
     * A filter for appending and validating custom headers.
     * <p>
     * On the client side, appends a new custom request header with a configured name and value to each outgoing request.
     * </p>
     * <p>
     * On the server side, validates that each request has a custom header with a configured name and value.
     * If the validation fails a HTTP 403 response is returned.
     * </p>
     */
    public static class CustomHeaderFilter implements ClientRequestFilter , ContainerRequestFilter {
        private final String headerName;

        private final String headerValue;

        public CustomHeaderFilter(String headerName, String headerValue) {
            if ((headerName == null) || (headerValue == null)) {
                throw new IllegalArgumentException("Header name and value must not be null.");
            }
            this.headerName = headerName;
            this.headerValue = headerValue;
        }

        @Override
        public void filter(ContainerRequestContext ctx) throws IOException {
            // validate
            if (!(headerValue.equals(ctx.getHeaderString(headerName)))) {
                ctx.abortWith(Response.status(FORBIDDEN).type(TEXT_PLAIN).entity(String.format("Expected header '%s' not present or value not equal to '%s'", headerName, headerValue)).build());
            }
        }

        @Override
        public void filter(ClientRequestContext ctx) throws IOException {
            // append
            ctx.getHeaders().putSingle(headerName, headerValue);
        }
    }

    /**
     * Internal resource accessed from the managed client resource.
     */
    @Path("internal")
    public static class InternalResource {
        @GET
        @Path("a")
        @ManagedClientTest.CustomHeaderFeature.Require(headerName = "custom-header", headerValue = "a")
        public String getA() {
            return "a";
        }

        @GET
        @Path("b")
        @ManagedClientTest.CustomHeaderFeature.Require(headerName = "custom-header", headerValue = "b")
        public String getB() {
            return "b";
        }
    }

    /**
     * A resource that uses managed clients to retrieve values of internal
     * resources 'A' and 'B', which are protected by a {@link CustomHeaderFilter}
     * and require a specific custom header in a request to be set to a specific value.
     * <p>
     * Properly configured managed clients have a {@code CustomHeaderFilter} instance
     * configured to insert the {@link CustomHeaderFeature.Require required} custom header
     * with a proper value into the outgoing client requests.
     * </p>
     */
    @Path("public")
    public static class PublicResource {
        // resolves to <base>/internal/a
        @Uri("a")
        @ManagedClientTest.ClientA
        private WebTarget targetA;

        @GET
        @Produces("text/plain")
        @Path("a")
        public String getTargetA() {
            return targetA.request(TEXT_PLAIN).get(String.class);
        }

        @GET
        @Produces("text/plain")
        @Path("b")
        public Response getTargetB(@Uri("internal/b")
        @ManagedClientTest.ClientB
        WebTarget targetB) {
            return targetB.request(TEXT_PLAIN).get();
        }
    }

    public static class MyClientAConfig extends ClientConfig {
        public MyClientAConfig() {
            register(new ManagedClientTest.CustomHeaderFilter("custom-header", "a"));
        }
    }

    public static class MyClientBConfig extends ClientConfig {
        public MyClientBConfig() {
            register(new ManagedClientTest.CustomHeaderFilter("custom-header", "b"));
        }
    }

    /**
     * Test that a connection via managed clients works properly.
     *
     * @throws Exception
     * 		in case of test failure.
     */
    @Test
    public void testManagedClient() throws Exception {
        final WebTarget resource = target().path("public").path("{name}");
        Response response;
        response = resource.resolveTemplate("name", "a").request(TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("a", response.readEntity(String.class));
        response = resource.resolveTemplate("name", "b").request(TEXT_PLAIN).get();
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("b", response.readEntity(String.class));
    }
}
