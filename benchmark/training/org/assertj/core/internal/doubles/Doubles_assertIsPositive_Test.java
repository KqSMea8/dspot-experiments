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


import org.assertj.core.api.Assertions;
import org.assertj.core.internal.DoublesBaseTest;
import org.assertj.core.test.TestData;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link Doubles#assertIsPositive(AssertionInfo, Double)}</code>.
 *
 * @author Alex Ruiz
 * @author Joel Costigliola
 */
public class Doubles_assertIsPositive_Test extends DoublesBaseTest {
    @Test
    public void should_succeed_since_actual_is_positive() {
        doubles.assertIsPositive(TestData.someInfo(), ((double) (6)));
    }

    @Test
    public void should_fail_since_actual_is_not_positive() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> doubles.assertIsPositive(someInfo(), (-6.0))).withMessage(String.format("%nExpecting:%n <-6.0>%nto be greater than:%n <0.0> "));
    }

    @Test
    public void should_succeed_since_actual_is_positive_according_to_absolute_value_comparison_strategy() {
        doublesWithAbsValueComparisonStrategy.assertIsPositive(TestData.someInfo(), 6.0);
    }

    @Test
    public void should_succeed_since_actual_is_positive_according_to_absolute_value_comparison_strategy2() {
        doublesWithAbsValueComparisonStrategy.assertIsPositive(TestData.someInfo(), (-6.0));
    }
}
