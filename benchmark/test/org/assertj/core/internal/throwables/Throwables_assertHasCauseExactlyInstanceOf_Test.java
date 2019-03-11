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
package org.assertj.core.internal.throwables;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldHaveCauseExactlyInstance;
import org.assertj.core.internal.ThrowablesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for
 * {@link org.assertj.core.internal.Throwables#assertHasCauseExactlyInstanceOf(org.assertj.core.api.AssertionInfo, Throwable, Class)}
 * .
 *
 * @author Jean-Christophe Gay
 */
public class Throwables_assertHasCauseExactlyInstanceOf_Test extends ThrowablesBaseTest {
    private Throwable throwableWithCause = new Throwable(new IllegalArgumentException());

    @Test
    public void should_pass_if_cause_is_exactly_instance_of_expected_type() {
        throwables.assertHasCauseExactlyInstanceOf(TestData.someInfo(), throwableWithCause, IllegalArgumentException.class);
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> throwables.assertHasCauseExactlyInstanceOf(someInfo(), null, .class)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_throw_NullPointerException_if_given_type_is_null() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> throwables.assertHasCauseExactlyInstanceOf(someInfo(), throwableWithCause, null)).withMessage("The given type should not be null");
    }

    @Test
    public void should_fail_if_actual_has_no_cause() {
        AssertionInfo info = TestData.someInfo();
        Class<NullPointerException> expectedCauseType = NullPointerException.class;
        try {
            throwables.assertHasCauseExactlyInstanceOf(info, ThrowablesBaseTest.actual, expectedCauseType);
        } catch (AssertionError err) {
            Mockito.verify(failures).failure(info, ShouldHaveCauseExactlyInstance.shouldHaveCauseExactlyInstance(ThrowablesBaseTest.actual, expectedCauseType));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_cause_is_not_instance_of_expected_type() {
        AssertionInfo info = TestData.someInfo();
        Class<NullPointerException> expectedCauseType = NullPointerException.class;
        try {
            throwables.assertHasCauseExactlyInstanceOf(info, throwableWithCause, expectedCauseType);
        } catch (AssertionError err) {
            Mockito.verify(failures).failure(info, ShouldHaveCauseExactlyInstance.shouldHaveCauseExactlyInstance(throwableWithCause, expectedCauseType));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_fail_if_cause_is_not_exactly_instance_of_expected_type() {
        AssertionInfo info = TestData.someInfo();
        Class<RuntimeException> expectedCauseType = RuntimeException.class;
        try {
            throwables.assertHasCauseExactlyInstanceOf(info, throwableWithCause, expectedCauseType);
        } catch (AssertionError err) {
            Mockito.verify(failures).failure(info, ShouldHaveCauseExactlyInstance.shouldHaveCauseExactlyInstance(throwableWithCause, expectedCauseType));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
