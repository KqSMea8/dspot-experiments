/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2019 the original author or authors.
 */
package org.assertj.core.api.zoneddatetime;


import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.assertj.core.api.AbstractZonedDateTimeAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.BaseTest;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;


public class ZonedDateTimeAssert_isEqualToIgnoringNanoseconds_Test extends BaseTest {
    private final ZonedDateTime refDatetime = ZonedDateTime.of(2000, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC);

    @Test
    public void should_pass_if_actual_is_equal_to_other_ignoring_nanosecond_fields() {
        Assertions.assertThat(refDatetime).isEqualToIgnoringNanos(refDatetime.withNano(55));
        Assertions.assertThat(refDatetime).isEqualToIgnoringNanos(refDatetime.plusNanos(1));
    }

    @Test
    public void should_fail_if_actual_is_not_equal_to_given_datetime_with_nanoseconds_ignored() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> assertThat(refDatetime).isEqualToIgnoringNanos(refDatetime.plusSeconds(1))).withMessage(String.format("%nExpecting:%n  <2000-01-01T00:00:01Z>%nto have same year, month, day, hour, minute and second as:%n  <2000-01-01T00:00:02Z>%nbut had not."));
    }

    @Test
    public void should_fail_as_seconds_fields_are_different_even_if_time_difference_is_less_than_a_second() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> assertThat(refDatetime).isEqualToIgnoringNanos(refDatetime.minusNanos(1))).withMessage(String.format("%nExpecting:%n  <2000-01-01T00:00:01Z>%nto have same year, month, day, hour, minute and second as:%n  <2000-01-01T00:00:00.999999999Z>%nbut had not."));
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
            ZonedDateTime actual = null;
            assertThat(actual).isEqualToIgnoringNanos(ZonedDateTime.now());
        }).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_throw_error_if_given_datetime_is_null() {
        Assertions.assertThatIllegalArgumentException().isThrownBy(() -> assertThat(refDatetime).isEqualToIgnoringNanos(null)).withMessage(AbstractZonedDateTimeAssert.NULL_DATE_TIME_PARAMETER_MESSAGE);
    }
}
