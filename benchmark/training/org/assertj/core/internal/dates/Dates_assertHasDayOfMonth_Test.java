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
package org.assertj.core.internal.dates;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldHaveDateField;
import org.assertj.core.internal.DatesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for <code>{@link Dates#assertHasDayOfMonth(AssertionInfo, Date, int)}</code>.
 *
 * @author Joel Costigliola
 */
public class Dates_assertHasDayOfMonth_Test extends DatesBaseTest {
    @Test
    public void should_fail_if_actual_has_not_given_day_of_month() {
        AssertionInfo info = TestData.someInfo();
        int day_of_month = 5;
        try {
            dates.assertHasDayOfMonth(info, actual, day_of_month);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldHaveDateField.shouldHaveDateField(actual, "day of month", day_of_month));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> dates.assertHasDayOfMonth(someInfo(), null, 1)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_pass_if_actual_has_given_day_of_month() {
        dates.assertHasDayOfMonth(TestData.someInfo(), actual, 1);
    }

    @Test
    public void should_fail_if_actual_has_not_given_day_of_month_whatever_custom_comparison_strategy_is() {
        AssertionInfo info = TestData.someInfo();
        int day_of_month = 5;
        try {
            datesWithCustomComparisonStrategy.assertHasDayOfMonth(info, actual, day_of_month);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldHaveDateField.shouldHaveDateField(actual, "day of month", day_of_month));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_actual_is_null_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> datesWithCustomComparisonStrategy.assertHasDayOfMonth(someInfo(), null, 1)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_pass_if_actual_has_given_day_of_month_whatever_custom_comparison_strategy_is() {
        datesWithCustomComparisonStrategy.assertHasDayOfMonth(TestData.someInfo(), actual, 1);
    }
}
