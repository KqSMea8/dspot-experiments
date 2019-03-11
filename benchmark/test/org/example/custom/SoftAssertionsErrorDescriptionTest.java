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
package org.example.custom;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;


public class SoftAssertionsErrorDescriptionTest {
    @Test
    public void should_display_the_error_cause_and_the_cause_first_stack_trace_elements() {
        // GIVEN
        SoftAssertions softly = new SoftAssertions();
        softly.fail("failure", SoftAssertionsErrorDescriptionTest.throwRuntimeException());
        // WHEN
        AssertionError error = Assertions.catchThrowableOfType(() -> softly.assertAll(), AssertionError.class);
        // THEN
        Assertions.assertThat(error).hasMessageStartingWith(String.format(("Multiple Failures (1 failure)%n" + ("\tfailure%n" + "at SoftAssertionsErrorDescriptionTest.should_display_the_error_cause_and_the_cause_first_stack_trace_elements(SoftAssertionsErrorDescriptionTest.java:28)"))));
    }
}
