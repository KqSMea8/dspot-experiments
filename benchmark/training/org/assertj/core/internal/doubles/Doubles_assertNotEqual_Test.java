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
package org.assertj.core.internal.doubles;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldNotBeEqual;
import org.assertj.core.internal.DoublesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for <code>{@link Doubles#assertNotEqual(AssertionInfo, Double, double)}</code>.
 *
 * @author Alex Ruiz
 * @author Joel Costigliola
 */
public class Doubles_assertNotEqual_Test extends DoublesBaseTest {
    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> doubles.assertNotEqual(someInfo(), null, 8.0)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_pass_if_doubles_are_not_equal() {
        doubles.assertNotEqual(TestData.someInfo(), 8.0, 6.0);
    }

    @Test
    public void should_fail_if_doubles_are_equal() {
        AssertionInfo info = TestData.someInfo();
        try {
            doubles.assertNotEqual(info, 6.0, 6.0);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldNotBeEqual.shouldNotBeEqual(6.0, 6.0));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_is_null_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> doublesWithAbsValueComparisonStrategy.assertNotEqual(someInfo(), null, 8.0)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_pass_if_doubles_are_not_equal_according_to_custom_comparison_strategy() {
        doublesWithAbsValueComparisonStrategy.assertNotEqual(TestData.someInfo(), 8.0, 6.0);
    }

    @Test
    public void should_fail_if_doubles_are_equal_according_to_custom_comparison_strategy() {
        AssertionInfo info = TestData.someInfo();
        try {
            doublesWithAbsValueComparisonStrategy.assertNotEqual(info, 6.0, (-6.0));
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldNotBeEqual.shouldNotBeEqual(6.0, (-6.0), absValueComparisonStrategy));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
