/**
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
package io.helidon.security.providers.httpsign;


import SecurityResponse.SecurityStatus.SUCCESS;
import io.helidon.common.CollectionsHelper;
import io.helidon.common.OptionalHelper;
import io.helidon.security.AuthenticationResponse;
import io.helidon.security.EndpointConfig;
import io.helidon.security.OutboundSecurityResponse;
import io.helidon.security.ProviderRequest;
import io.helidon.security.SecurityContext;
import io.helidon.security.SecurityEnvironment;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.net.URI.create;


/**
 * Unit test for {@link HttpSignProvider}.
 */
public abstract class HttpSignProviderTest {
    @Test
    public void testInboundSignatureRsa() throws InterruptedException, ExecutionException {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("Signature", CollectionsHelper.listOf(("keyId=\"rsa-key-12345\",algorithm=\"rsa-sha256\",headers=\"date " + ((((((("host (request-target) authorization\"," + "signature=\"Rm5PjuUdJ927esGQ2gm/6QBEM9IM7J5qSZuP8NV8+GXUf") + "boUV6ST2EYLYniFGt5/3BO/2+vqQdqezdTVPr/JCwqBx+9T9ZynG7YqRj") + "KvXzcmvQOu5vQmCK5x/HR0fXU41Pjq+jywsD0k6KdxF6TWr6tvWRbwFet") + "+YSb0088o/65Xeqghw7s0vShf7jPZsaaIHnvM9SjWgix9VvpdEn4NDvqh") + "ebieVD3Swb1VG5+/7ECQ9VAlX30U5/jQ5hPO3yuvRlg5kkMjJiN7tf/68") + "If/5O2Z4H+7VmW0b1U69/JoOQJA0av1gCX7HVfa/YTCxIK4UFiI6h963q") + "2x7LSkqhdWGA==\""))));
        headers.put("host", CollectionsHelper.listOf("example.org"));
        headers.put("date", CollectionsHelper.listOf("Thu, 08 Jun 2014 18:32:30 GMT"));
        headers.put("authorization", CollectionsHelper.listOf("basic dXNlcm5hbWU6cGFzc3dvcmQ="));
        HttpSignProvider provider = getProvider();
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.executorService()).thenReturn(ForkJoinPool.commonPool());
        SecurityEnvironment se = SecurityEnvironment.builder().path("/my/resource").headers(headers).build();
        EndpointConfig ep = EndpointConfig.create();
        ProviderRequest request = Mockito.mock(ProviderRequest.class);
        Mockito.when(request.securityContext()).thenReturn(context);
        Mockito.when(request.env()).thenReturn(se);
        Mockito.when(request.endpointConfig()).thenReturn(ep);
        AuthenticationResponse atnResponse = provider.authenticate(request).toCompletableFuture().get();
        MatcherAssert.assertThat(atnResponse.description().orElse("Unknown problem"), atnResponse.status(), CoreMatchers.is(SUCCESS));
        OptionalHelper.from(atnResponse.user().map(Subject::principal)).ifPresentOrElse(( principal) -> {
            assertThat(principal.getName(), is("aUser"));
            assertThat(principal.abacAttribute(HttpSignProvider.ATTRIB_NAME_KEY_ID), is(Optional.of("rsa-key-12345")));
        }, () -> fail("User must be filled"));
    }

    @Test
    public void testInboundSignatureHmac() throws InterruptedException, ExecutionException {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("Signature", CollectionsHelper.listOf(("keyId=\"myServiceKeyId\",algorithm=\"hmac-sha256\",headers=\"date host (request-target) " + ("authorization\"," + "signature=\"0BcQq9TckrtGvlpHiMxNqMq0vW6dPVTGVDUVDrGwZyI=\""))));
        headers.put("host", CollectionsHelper.listOf("example.org"));
        headers.put("date", CollectionsHelper.listOf("Thu, 08 Jun 2014 18:32:30 GMT"));
        headers.put("authorization", CollectionsHelper.listOf("basic dXNlcm5hbWU6cGFzc3dvcmQ="));
        HttpSignProvider provider = getProvider();
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.executorService()).thenReturn(ForkJoinPool.commonPool());
        SecurityEnvironment se = SecurityEnvironment.builder().path("/my/resource").headers(headers).build();
        EndpointConfig ep = EndpointConfig.create();
        ProviderRequest request = Mockito.mock(ProviderRequest.class);
        Mockito.when(request.securityContext()).thenReturn(context);
        Mockito.when(request.env()).thenReturn(se);
        Mockito.when(request.endpointConfig()).thenReturn(ep);
        AuthenticationResponse atnResponse = provider.authenticate(request).toCompletableFuture().get();
        MatcherAssert.assertThat(atnResponse.description().orElse("Unknown problem"), atnResponse.status(), CoreMatchers.is(SUCCESS));
        OptionalHelper.from(atnResponse.service().map(Subject::principal)).ifPresentOrElse(( principal) -> {
            assertThat(principal.getName(), is("aSetOfTrustedServices"));
            assertThat(principal.abacAttribute(HttpSignProvider.ATTRIB_NAME_KEY_ID), is(Optional.of("myServiceKeyId")));
        }, () -> fail("User must be filled"));
    }

    @Test
    public void testOutboundSignatureRsa() throws InterruptedException, ExecutionException {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // the generated host contains port as well, so we must explicitly define it here
        headers.put("host", CollectionsHelper.listOf("example.org"));
        headers.put("date", CollectionsHelper.listOf("Thu, 08 Jun 2014 18:32:30 GMT"));
        headers.put("authorization", CollectionsHelper.listOf("basic dXNlcm5hbWU6cGFzc3dvcmQ="));
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.executorService()).thenReturn(ForkJoinPool.commonPool());
        ProviderRequest request = Mockito.mock(ProviderRequest.class);
        Mockito.when(request.securityContext()).thenReturn(context);
        SecurityEnvironment outboundEnv = SecurityEnvironment.builder().path("/my/resource").targetUri(create("http://example.org/my/resource")).headers(headers).build();
        EndpointConfig outboundEp = EndpointConfig.create();
        boolean outboundSupported = getProvider().isOutboundSupported(request, outboundEnv, outboundEp);
        MatcherAssert.assertThat("Outbound should be supported", outboundSupported, CoreMatchers.is(true));
        OutboundSecurityResponse response = getProvider().outboundSecurity(request, outboundEnv, outboundEp).toCompletableFuture().get();
        MatcherAssert.assertThat(response.status(), CoreMatchers.is(SUCCESS));
        Map<String, List<String>> updatedHeaders = response.requestHeaders();
        MatcherAssert.assertThat(updatedHeaders, CoreMatchers.notNullValue());
        // and now the value
        validateSignatureHeader(outboundEnv, updatedHeaders.get("Signature").iterator().next(), "rsa-key-12345", "rsa-sha256", CollectionsHelper.listOf("date", "host", SignedHeadersConfig.REQUEST_TARGET, "authorization"), ("Rm5PjuUdJ927esGQ2gm/6QBEM9IM7J5qSZuP8NV8+GXUf" + ((((("boUV6ST2EYLYniFGt5/3BO/2+vqQdqezdTVPr/JCwqBx+9T9ZynG7YqRj" + "KvXzcmvQOu5vQmCK5x/HR0fXU41Pjq+jywsD0k6KdxF6TWr6tvWRbwFet") + "+YSb0088o/65Xeqghw7s0vShf7jPZsaaIHnvM9SjWgix9VvpdEn4NDvqh") + "ebieVD3Swb1VG5+/7ECQ9VAlX30U5/jQ5hPO3yuvRlg5kkMjJiN7tf/68") + "If/5O2Z4H+7VmW0b1U69/JoOQJA0av1gCX7HVfa/YTCxIK4UFiI6h963q") + "2x7LSkqhdWGA==")));
    }

    @Test
    public void testOutboundSignatureHmac() throws InterruptedException, ExecutionException {
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        // the generated host contains port as well, so we must explicitly define it here
        headers.put("host", CollectionsHelper.listOf("localhost"));
        headers.put("date", CollectionsHelper.listOf("Thu, 08 Jun 2014 18:32:30 GMT"));
        SecurityContext context = Mockito.mock(SecurityContext.class);
        Mockito.when(context.executorService()).thenReturn(ForkJoinPool.commonPool());
        ProviderRequest request = Mockito.mock(ProviderRequest.class);
        Mockito.when(request.securityContext()).thenReturn(context);
        SecurityEnvironment outboundEnv = SecurityEnvironment.builder().path("/second/someOtherPath").targetUri(create("http://localhost/second/someOtherPath")).headers(headers).build();
        EndpointConfig outboundEp = EndpointConfig.create();
        boolean outboundSupported = getProvider().isOutboundSupported(request, outboundEnv, outboundEp);
        MatcherAssert.assertThat("Outbound should be supported", outboundSupported, CoreMatchers.is(true));
        OutboundSecurityResponse response = getProvider().outboundSecurity(request, outboundEnv, outboundEp).toCompletableFuture().get();
        MatcherAssert.assertThat(response.status(), CoreMatchers.is(SUCCESS));
        Map<String, List<String>> updatedHeaders = response.requestHeaders();
        MatcherAssert.assertThat(updatedHeaders, CoreMatchers.notNullValue());
        // and now the value
        validateSignatureHeader(outboundEnv, updatedHeaders.get("Signature").iterator().next(), "myServiceKeyId", "hmac-sha256", CollectionsHelper.listOf("date", SignedHeadersConfig.REQUEST_TARGET, "host"), "SkeKVi6BoUd2/aUfXyIVIFAKEkKp7sg2KsS1UieB/+E=");
    }
}
