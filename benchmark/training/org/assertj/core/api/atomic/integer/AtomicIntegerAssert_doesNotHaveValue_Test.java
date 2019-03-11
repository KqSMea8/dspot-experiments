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
package org.assertj.core.api.atomic.integer;


import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldNotContainValue;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;


public class AtomicIntegerAssert_doesNotHaveValue_Test {
    @Test
    public void should_pass_when_actual_does_not_have_the_expected_value() {
        AtomicInteger actual = new AtomicInteger(123);
        Assertions.assertThat(actual).doesNotHaveValue(456);
    }

    @Test
    public void should_fail_when_actual_has_the_expected_value() {
        int value = 123;
        AtomicInteger actual = new AtomicInteger(value);
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> assertThat(actual).doesNotHaveValue(value)).withMessage(ShouldNotContainValue.shouldNotContainValue(actual, value).create());
    }

    @Test
    public void should_fail_when_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> {
            AtomicInteger actual = null;
            assertThat(actual).doesNotHaveValue(1234);
        }).withMessage(FailureMessages.actualIsNull());
    }
}
