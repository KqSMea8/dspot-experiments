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
package org.assertj.core.api.doublearray;


import org.assertj.core.api.Assertions;
import org.assertj.core.api.DoubleArrayAssertBaseTest;
import org.assertj.core.data.Index;
import org.assertj.core.test.DoubleArrays;
import org.assertj.core.test.TestData;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link DoubleArrayAssert#contains(double, Index)}</code>.
 *
 * @author Alex Ruiz
 */
public class DoubleArrayAssert_contains_at_Index_Test extends DoubleArrayAssertBaseTest {
    private final Index index = TestData.someIndex();

    @Test
    public void should_pass_with_precision_specified_as_last_argument() {
        // GIVEN
        double[] actual = DoubleArrays.arrayOf(1.0, 2.0);
        // THEN
        Assertions.assertThat(actual).contains(1.0, Assertions.atIndex(0), Assertions.withPrecision(0.1));
    }

    @Test
    public void should_pass_with_precision_specified_in_comparator() {
        // GIVEN
        double[] actual = DoubleArrays.arrayOf(1.0, 2.0);
        // THEN
        Assertions.assertThat(actual).usingComparatorWithPrecision(0.1).contains(2.05, Assertions.atIndex(1));
    }
}
