package org.baeldung.web.interceptor;


import org.baeldung.security.spring.SecurityWithoutCsrfConfig;
import org.baeldung.spring.MvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SecurityWithoutCsrfConfig.class, MvcConfig.class })
public class LoggerInterceptorIntegrationTest {
    @Autowired
    WebApplicationContext wac;

    @Autowired
    MockHttpSession session;

    private MockMvc mockMvc;

    /**
     * After execution of HTTP GET logs from interceptor will be displayed in
     * the console
     *
     * @throws Exception
     * 		
     */
    @Test
    public void testInterceptors() throws Exception {
        mockMvc.perform(get("/login.html")).andExpect(status().isOk());
    }
}
