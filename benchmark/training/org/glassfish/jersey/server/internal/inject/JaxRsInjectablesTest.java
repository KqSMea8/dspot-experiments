/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.server.internal.inject;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Test;


/**
 * Covers JAX-RS defined @Context injectable values including
 * proxy support for these.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JaxRsInjectablesTest extends AbstractTest {
    @Path("/")
    public static class PerRequestContextResource {
        @Context
        UriInfo ui;

        @Context
        HttpHeaders hs;

        @Context
        Request r;

        @Context
        SecurityContext sc;

        @GET
        public String get() {
            Assert.assertNotNull(ui);
            Assert.assertNotNull(hs);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
            return "GET";
        }
    }

    @Path("/")
    public static class PerRequestContextConstructorParameterResource {
        public PerRequestContextConstructorParameterResource(@Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r, @Context
        SecurityContext sc) {
            Assert.assertNotNull(ui);
            Assert.assertNotNull(hs);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
        }

        @GET
        public String get() {
            return "GET";
        }
    }

    @Path("/")
    public static class PerRequestContextMethodParameterResource {
        @GET
        public String get(@Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r, @Context
        SecurityContext sc) {
            Assert.assertNotNull(ui);
            Assert.assertNotNull(hs);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
            return "GET";
        }
    }

    @Path("/")
    @Singleton
    public static class SingletonContextResource {
        @Context
        UriInfo ui;

        @Context
        HttpHeaders hs;

        @Context
        Request r;

        @Context
        SecurityContext sc;

        @GET
        public String get() {
            Assert.assertNotNull(hs);
            Assert.assertNotNull(ui);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
            return "GET";
        }
    }

    @Path("/")
    @Singleton
    public static class SingletonContextConstructorParameterResource {
        public SingletonContextConstructorParameterResource(@Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r, @Context
        SecurityContext sc) {
            Assert.assertNotNull(hs);
            Assert.assertNotNull(ui);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
        }

        @GET
        public String get() {
            return "GET";
        }
    }

    @Test
    public void testPerRequestInjected() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.PerRequestContextResource.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testPerRequestConstructorParameterInjected() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.PerRequestContextConstructorParameterResource.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testPerRequestMethodParameterInjected() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.PerRequestContextMethodParameterResource.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testSingletonInjected() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.SingletonContextResource.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testSingletonConstructorParameterInjected() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.SingletonContextConstructorParameterResource.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Path("/")
    public static class StringWriterResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    @Provider
    @Consumes({ "text/plain", "*/*" })
    @Produces({ "text/plain", "*/*" })
    public static class StringWriterField implements MessageBodyWriter<String> {
        public StringWriterField() {
            int i = 0;
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == (String.class);
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            Assert.assertNotNull(ui);
            Assert.assertNotNull(hs);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
            arg6.write(arg0.getBytes());
        }

        @Context
        UriInfo ui;

        @Context
        HttpHeaders hs;

        @Context
        Request r;

        @Context
        SecurityContext sc;
    }

    @Provider
    @Consumes({ "text/plain", "*/*" })
    @Produces({ "text/plain", "*/*" })
    public static class StringWriterConstructor implements MessageBodyWriter<String> {
        public StringWriterConstructor(@Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r, @Context
        SecurityContext sc) {
            Assert.assertNotNull(ui);
            Assert.assertNotNull(hs);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == (String.class);
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }
    }

    @Provider
    @Consumes({ "text/plain", "*/*" })
    @Produces({ "text/plain", "*/*" })
    public static class StringWriterMutlipleConstructor implements MessageBodyWriter<String> {
        public StringWriterMutlipleConstructor(@Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r, @Context
        SecurityContext sc) {
            Assert.assertNotNull(ui);
            Assert.assertNotNull(hs);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
        }

        public StringWriterMutlipleConstructor() {
            Assert.fail();
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == (String.class);
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }
    }

    @Provider
    @Consumes({ "text/plain", "*/*" })
    @Produces({ "text/plain", "*/*" })
    public static class StringWriterMutliplePartialConstructor implements MessageBodyWriter<String> {
        public StringWriterMutliplePartialConstructor(@Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r, @Context
        SecurityContext sc) {
            Assert.assertNotNull(ui);
            Assert.assertNotNull(hs);
            Assert.assertNotNull(r);
            Assert.assertNotNull(sc);
        }

        public StringWriterMutliplePartialConstructor(String rc, @Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r) {
            Assert.fail();
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == (String.class);
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }
    }

    @Test
    public void testProviderField() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.StringWriterResource.class, JaxRsInjectablesTest.StringWriterField.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderInstanceField() throws Exception {
        ResourceConfig rc = new ResourceConfig(JaxRsInjectablesTest.StringWriterResource.class);
        rc.register(new JaxRsInjectablesTest.StringWriterField());
        initiateWebApplication(rc);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderConstructor() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.StringWriterResource.class, JaxRsInjectablesTest.StringWriterConstructor.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderMultipleConstructor() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.StringWriterResource.class, JaxRsInjectablesTest.StringWriterMutlipleConstructor.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Test
    public void testProviderMultiplePartialConstructor() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.StringWriterResource.class, JaxRsInjectablesTest.StringWriterMutliplePartialConstructor.class);
        Assert.assertEquals("GET", resource("/").getEntity());
    }

    @Path("/{p}")
    public static class PerRequestFieldResource {
        @PathParam("p")
        String p;

        @QueryParam("q")
        String q;

        @GET
        public String get() {
            return (p) + (q);
        }
    }

    @Test
    public void testPerRequestParamInjected() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.PerRequestFieldResource.class);
        Assert.assertEquals("foobar", resource("/foo?q=bar").getEntity());
    }

    @Provider
    @Produces("text/plain")
    public static class StringWriterParamConstructor implements MessageBodyWriter<String> {
        static int illegalStateExceptionCount = 0;

        static int runtimeExceptionCount = 0;

        public StringWriterParamConstructor(@Context
        UriInfo ui, @Context
        HttpHeaders hs, @Context
        Request r, @Context
        SecurityContext sc) {
            try {
                ui.getAbsolutePath();
            } catch (IllegalStateException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.illegalStateExceptionCount)++;
            } catch (RuntimeException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.runtimeExceptionCount)++;
            }
            try {
                hs.getDate();
            } catch (IllegalStateException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.illegalStateExceptionCount)++;
            } catch (RuntimeException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.runtimeExceptionCount)++;
            }
            try {
                r.getMethod();
            } catch (IllegalStateException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.illegalStateExceptionCount)++;
            } catch (RuntimeException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.runtimeExceptionCount)++;
            }
            try {
                sc.getAuthenticationScheme();
            } catch (IllegalStateException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.illegalStateExceptionCount)++;
            } catch (RuntimeException ex) {
                (JaxRsInjectablesTest.StringWriterParamConstructor.runtimeExceptionCount)++;
            }
        }

        @Override
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == (String.class);
        }

        @Override
        public long getSize(String arg0, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream os) throws IOException, WebApplicationException {
            String s = ((((JaxRsInjectablesTest.StringWriterParamConstructor.illegalStateExceptionCount) + " ") + (JaxRsInjectablesTest.StringWriterParamConstructor.runtimeExceptionCount)) + " ") + arg0;
            os.write(s.getBytes());
        }
    }

    @Path("/")
    public static class StringWriterParamConstructorResource {
        @GET
        @Produces("text/plain")
        public String get() {
            return (((JaxRsInjectablesTest.StringWriterParamConstructor.illegalStateExceptionCount) + " ") + (JaxRsInjectablesTest.StringWriterParamConstructor.runtimeExceptionCount)) + " GET";
        }
    }

    @Test
    public void testStringWriterParamConstructor() throws Exception {
        initiateWebApplication(JaxRsInjectablesTest.StringWriterParamConstructorResource.class, JaxRsInjectablesTest.StringWriterParamConstructor.class);
        Assert.assertEquals("4 0 GET", resource("/").getEntity());
    }
}
