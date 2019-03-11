package org.baeldung.web.test;


import HttpMethod.GET;
import HttpMethod.PUT;
import KryoHttpMessageConverter.KRYO;
import MediaType.APPLICATION_JSON;
import MediaType.APPLICATION_XML;
import ProtobufHttpMessageConverter.PROTOBUF;
import java.util.Arrays;
import org.baeldung.config.converter.KryoHttpMessageConverter;
import org.baeldung.web.dto.Foo;
import org.baeldung.web.dto.FooProtos;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.client.RestTemplate;


/**
 * Integration Test class. Tests methods hits the server's rest services.
 */
public class SpringHttpMessageConvertersLiveTest {
    private static String BASE_URI = "http://localhost:8082/spring-rest/";

    /**
     * Without specifying Accept Header, uses the default response from the
     * server (in this case json)
     */
    @Test
    public void whenRetrievingAFoo_thenCorrect() {
        final String URI = (SpringHttpMessageConvertersLiveTest.BASE_URI) + "foos/{id}";
        final RestTemplate restTemplate = new RestTemplate();
        final Foo resource = restTemplate.getForObject(URI, Foo.class, "1");
        Assert.assertThat(resource, Matchers.notNullValue());
    }

    @Test
    public void givenConsumingXml_whenReadingTheFoo_thenCorrect() {
        final String URI = (SpringHttpMessageConvertersLiveTest.BASE_URI) + "foos/{id}";
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(APPLICATION_XML));
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        final ResponseEntity<Foo> response = restTemplate.exchange(URI, GET, entity, Foo.class, "1");
        final Foo resource = response.getBody();
        Assert.assertThat(resource, Matchers.notNullValue());
    }

    @Test
    public void givenConsumingJson_whenReadingTheFoo_thenCorrect() {
        final String URI = (SpringHttpMessageConvertersLiveTest.BASE_URI) + "foos/{id}";
        final RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(APPLICATION_JSON));
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        final ResponseEntity<Foo> response = restTemplate.exchange(URI, GET, entity, Foo.class, "1");
        final Foo resource = response.getBody();
        Assert.assertThat(resource, Matchers.notNullValue());
    }

    @Test
    public void givenConsumingXml_whenWritingTheFoo_thenCorrect() {
        final String URI = (SpringHttpMessageConvertersLiveTest.BASE_URI) + "foos/{id}";
        final RestTemplate restTemplate = new RestTemplate();
        final Foo resource = new Foo(4, "jason");
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(APPLICATION_JSON));
        headers.setContentType(APPLICATION_XML);
        final HttpEntity<Foo> entity = new HttpEntity<Foo>(resource, headers);
        final ResponseEntity<Foo> response = restTemplate.exchange(URI, PUT, entity, Foo.class, resource.getId());
        final Foo fooResponse = response.getBody();
        Assert.assertEquals(resource.getId(), fooResponse.getId());
    }

    @Test
    public void givenConsumingProtobuf_whenReadingTheFoo_thenCorrect() {
        final String URI = (SpringHttpMessageConvertersLiveTest.BASE_URI) + "foos/{id}";
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(new ProtobufHttpMessageConverter()));
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(PROTOBUF));
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        final ResponseEntity<FooProtos.Foo> response = restTemplate.exchange(URI, GET, entity, Foo.class, "1");
        final FooProtos.Foo resource = response.getBody();
        Assert.assertThat(resource, Matchers.notNullValue());
    }

    @Test
    public void givenConsumingKryo_whenReadingTheFoo_thenCorrect() {
        final String URI = (SpringHttpMessageConvertersLiveTest.BASE_URI) + "foos/{id}";
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Arrays.asList(new KryoHttpMessageConverter()));
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(KRYO));
        final HttpEntity<String> entity = new HttpEntity<String>(headers);
        final ResponseEntity<Foo> response = restTemplate.exchange(URI, GET, entity, Foo.class, "1");
        final Foo resource = response.getBody();
        Assert.assertThat(resource, Matchers.notNullValue());
    }
}
