/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.tests.e2e.common;


import Status.UNSUPPORTED_MEDIA_TYPE;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test case for unsupported media type.
 *
 * @author Miroslav Fuksa
 */
public class MessageBodyReaderUnsupportedTest extends JerseyTest {
    /**
     * Send request to with application/json content to server where JsonJaxbBinder is not registered. UNSUPPORTED_MEDIA_TYPE
     * should be returned.
     */
    @Test
    public void testUnsupportedMessageBodyReader() {
        client().register(new MessageBodyReaderUnsupportedTest.TestEntityProvider());
        MessageBodyReaderUnsupportedTest.TestEntity entity = new MessageBodyReaderUnsupportedTest.TestEntity("testEntity");
        Response response = target().path("test").request(MessageBodyReaderUnsupportedTest.TestEntityProvider.TEST_ENTITY_TYPE).post(Entity.entity(entity, MessageBodyReaderUnsupportedTest.TestEntityProvider.TEST_ENTITY_TYPE));
        // TestEntityProvider is not registered on the server and therefore the server should return UNSUPPORTED_MEDIA_TYPE
        Assert.assertEquals(UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
        Assert.assertFalse(MessageBodyReaderUnsupportedTest.Resource.methodCalled);
        String responseEntity = response.readEntity(String.class);
        Assert.assertTrue(((responseEntity == null) || ((responseEntity.length()) == 0)));
    }

    /**
     * Test Resource class.
     *
     * @author Miroslav Fuksa
     */
    @Path("test")
    public static class Resource {
        private static volatile boolean methodCalled;

        /**
         * Resource method producing a {@code null} result.
         *
         * @param entity
         * 		test entity.
         * @return {@code null}.
         */
        @POST
        @Produces(MessageBodyReaderUnsupportedTest.TestEntityProvider.TEST_ENTITY)
        @Consumes(MessageBodyReaderUnsupportedTest.TestEntityProvider.TEST_ENTITY)
        @SuppressWarnings("UnusedParameters")
        public MessageBodyReaderUnsupportedTest.TestEntity processEntityAndProduceNull(MessageBodyReaderUnsupportedTest.TestEntity entity) {
            MessageBodyReaderUnsupportedTest.Resource.methodCalled = true;
            return null;
        }
    }

    /**
     * Test bean.
     *
     * @author Miroslav Fuksa
     */
    public static class TestEntity {
        private final String value;

        /**
         * Get value.
         *
         * @return value.
         */
        public String getValue() {
            return value;
        }

        /**
         * Create new test entity.
         *
         * @param value
         * 		entity value.
         */
        public TestEntity(String value) {
            super();
            this.value = value;
        }
    }

    /**
     * Custom test entity provider.
     */
    @Produces("test/entity")
    @Consumes("test/entity")
    public static class TestEntityProvider implements MessageBodyReader<MessageBodyReaderUnsupportedTest.TestEntity> , MessageBodyWriter<MessageBodyReaderUnsupportedTest.TestEntity> {
        /**
         * Test bean media type string.
         */
        public static final String TEST_ENTITY = "test/entity";

        /**
         * Test bean media type.
         */
        public static final MediaType TEST_ENTITY_TYPE = MediaType.valueOf(MessageBodyReaderUnsupportedTest.TestEntityProvider.TEST_ENTITY);

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return ((MessageBodyReaderUnsupportedTest.TestEntity.class) == type) && (MessageBodyReaderUnsupportedTest.TestEntityProvider.TEST_ENTITY_TYPE.equals(mediaType));
        }

        @Override
        public MessageBodyReaderUnsupportedTest.TestEntity readFrom(Class<MessageBodyReaderUnsupportedTest.TestEntity> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
            return new MessageBodyReaderUnsupportedTest.TestEntity(ReaderWriter.readFromAsString(entityStream, mediaType));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return ((MessageBodyReaderUnsupportedTest.TestEntity.class) == type) && (MessageBodyReaderUnsupportedTest.TestEntityProvider.TEST_ENTITY_TYPE.equals(mediaType));
        }

        @Override
        public long getSize(MessageBodyReaderUnsupportedTest.TestEntity testEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(MessageBodyReaderUnsupportedTest.TestEntity testEntity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            ReaderWriter.writeToAsString(testEntity.getValue(), entityStream, mediaType);
        }
    }
}
