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
package org.assertj.core.internal.iterables;


import java.util.Collection;
import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldHaveSize;
import org.assertj.core.internal.IterablesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.util.FailureMessages;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Iterables#assertHasSize(org.assertj.core.api.AssertionInfo, Iterable, int)}</code>.
 *
 * @author Alex Ruiz
 * @author Joel Costigliola
 */
public class Iterables_assertHasSize_Test extends IterablesBaseTest {
    @Test
    public void should_pass_if_size_of_actual_is_equal_to_expected_size() {
        iterables.assertHasSize(TestData.someInfo(), Lists.newArrayList("Luke", "Yoda"), 2);
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> iterables.assertHasSize(someInfo(), null, 8)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_size_of_actual_is_not_equal_to_expected_size() {
        AssertionInfo info = TestData.someInfo();
        Collection<String> actual = Lists.newArrayList("Yoda");
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> iterables.assertHasSize(info, actual, 8)).withMessage(ShouldHaveSize.shouldHaveSize(actual, actual.size(), 8).create());
    }

    @Test
    public void should_pass_if_size_of_actual_is_equal_to_expected_size_whatever_custom_comparison_strategy_is() {
        iterablesWithCaseInsensitiveComparisonStrategy.assertHasSize(TestData.someInfo(), Lists.newArrayList("Luke", "Yoda"), 2);
    }

    @Test
    public void should_fail_if_actual_is_null_whatever_custom_comparison_strategy_is() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> iterablesWithCaseInsensitiveComparisonStrategy.assertHasSize(someInfo(), null, 8)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_size_of_actual_is_not_equal_to_expected_size_whatever_custom_comparison_strategy_is() {
        AssertionInfo info = TestData.someInfo();
        Collection<String> actual = Lists.newArrayList("Yoda");
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> iterablesWithCaseInsensitiveComparisonStrategy.assertHasSize(info, actual, 8)).withMessage(ShouldHaveSize.shouldHaveSize(actual, actual.size(), 8).create());
    }
}
