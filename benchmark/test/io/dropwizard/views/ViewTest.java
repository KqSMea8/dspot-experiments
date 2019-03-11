package io.dropwizard.views;


import org.junit.jupiter.api.Test;


public class ViewTest {
    private final View view = new View("/blah.tmp") {};

    @Test
    public void hasATemplate() throws Exception {
        assertThat(view.getTemplateName()).isEqualTo("/blah.tmp");
    }
}
