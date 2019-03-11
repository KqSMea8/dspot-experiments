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
package org.glassfish.jersey.tests.e2e.server;


import java.util.concurrent.Executors;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;


/**
 * Injection E2E tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class InjectionTest extends JerseyTest {
    @Path("injection")
    public static class InjectionTestResource {
        @DELETE
        @Path("delete-path-param/{id}")
        public String deletePathParam(String body, @PathParam("id")
        String id) {
            return (("deleted: " + id) + "-") + body;
        }

        @DELETE
        @Path("delete-path-param-async/{id}")
        public void deletePathParam(String body, @PathParam("id")
        String id, @Suspended
        AsyncResponse ar) {
            ar.resume(((("deleted: " + id) + "-") + body));
        }

        @GET
        @Path("async")
        public void asyncGet(@Context
        final UriInfo uriInfo, @Context
        final Request request, @Context
        final HttpHeaders headers, @Context
        final SecurityContext securityContext, @Suspended
        final AsyncResponse response) {
            // now suspend and resume later on with
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        response.resume(String.format("base uri: %s\nheaders: %s\nmethod: %s\nprincipal: %s", uriInfo.getBaseUriBuilder().build(), headers.getRequestHeaders(), request.getMethod(), securityContext.getUserPrincipal()));
                    } catch (Throwable e) {
                        response.resume(e);
                    }
                }
            });
        }
    }

    /**
     * JERSEY-1761 reproducer.
     *
     * This is to make sure no proxy gets injected into async method parameters.
     */
    @Test
    public void testAsyncMethodParamInjection() {
        Response response = target("injection").path("async").request().get();
        Assert.assertEquals("Unexpected response status.", 200, response.getStatus());
        Assert.assertNotNull("Response is null.", response);
    }
}
