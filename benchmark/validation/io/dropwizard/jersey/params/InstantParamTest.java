package io.dropwizard.jersey.params;


import io.dropwizard.jersey.errors.ErrorMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;


public class InstantParamTest {
    @Test
    public void parsesDateTimes() {
        final InstantParam param = new InstantParam("2012-11-19T00:00:00Z");
        Instant instant = LocalDateTime.of(2012, 11, 19, 0, 0).toInstant(ZoneOffset.UTC);
        assertThat(param.get()).isEqualTo(instant);
    }

    @Test
    public void nullThrowsAnException() {
        assertThatThrownBy(() -> new InstantParam(null, "myDate")).isInstanceOfSatisfying(WebApplicationException.class, ( e) -> {
            assertThat(e.getResponse().getStatus()).isEqualTo(400);
            assertThat(e.getResponse().getEntity()).isEqualTo(new ErrorMessage(400, "myDate must be in a ISO-8601 format."));
        });
    }
}
