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
package org.assertj.core.internal.strings;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldHaveLineCount;
import org.assertj.core.internal.StringsBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link org.assertj.core.internal.Strings#assertHasLineCount(org.assertj.core.api.AssertionInfo, CharSequence, int)}</code>.
 *
 * @author Mariusz Smykula
 */
public class Strings_assertHasLinesCount_Test extends StringsBaseTest {
    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> strings.assertHasLineCount(someInfo(), null, 3)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_lines_count_of_actual_is_not_equal_to_expected_size() {
        AssertionInfo info = TestData.someInfo();
        String actual = ("Begin" + (System.lineSeparator())) + "End";
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> strings.assertHasLineCount(info, actual, 6)).withMessage(ShouldHaveLineCount.shouldHaveLinesCount(actual, 2, 6).create());
    }

    @Test
    public void should_pass_if_lines_count_of_actual_is_equal_to_expected_lines_count() {
        strings.assertHasLineCount(TestData.someInfo(), String.format((("Begin" + (System.lineSeparator())) + "Middle%nEnd")), 3);
    }

    @Test
    public void should_fail_if_actual_is_null_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> stringsWithCaseInsensitiveComparisonStrategy.assertHasLineCount(someInfo(), null, 3)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_lines_count_of_actual_is_not_equal_to_expected_lines_count_whatever_custom_comparison_strategy_is() {
        AssertionInfo info = TestData.someInfo();
        String actual = ("Begin" + (System.lineSeparator())) + "End";
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> stringsWithCaseInsensitiveComparisonStrategy.assertHasLineCount(info, actual, 3)).withMessage(ShouldHaveLineCount.shouldHaveLinesCount(actual, 2, 3).create());
    }

    @Test
    public void should_pass_if_lines_count_of_actual_is_equal_to_expected_lines_count_whatever_custom_comparison_strategy_is() {
        stringsWithCaseInsensitiveComparisonStrategy.assertHasLineCount(TestData.someInfo(), (("Begin" + (System.lineSeparator())) + "End"), 2);
    }
}
