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
package org.assertj.core.internal.maps;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldNotContainKey;
import org.assertj.core.internal.MapsBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for <code>{@link Maps#assertDoesNotContainKey(AssertionInfo, Map, Object)}</code>.
 *
 * @author Nicolas Fran?ois
 * @author Joel Costigliola
 */
public class Maps_assertDoesNotContainKey_Test extends MapsBaseTest {
    @Test
    public void should_pass_if_actual_contains_given_key() {
        maps.assertDoesNotContainKey(TestData.someInfo(), actual, "power");
    }

    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> maps.assertDoesNotContainKey(someInfo(), null, "power")).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_success_if_key_is_null() {
        maps.assertDoesNotContainKey(TestData.someInfo(), actual, null);
    }

    @Test
    public void should_fail_if_actual_does_not_contain_key() {
        AssertionInfo info = TestData.someInfo();
        String key = "name";
        try {
            maps.assertDoesNotContainKey(info, actual, key);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldNotContainKey.shouldNotContainKey(actual, key));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
