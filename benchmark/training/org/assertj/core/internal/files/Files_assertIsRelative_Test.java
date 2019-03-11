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
package org.assertj.core.internal.files;


import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.ShouldBeRelativePath;
import org.assertj.core.internal.FilesBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


/**
 * Tests for <code>{@link Files#assertIsRelative(AssertionInfo, File)}</code>.
 *
 * @author Yvonne Wang
 * @author Joel Costigliola
 */
public class Files_assertIsRelative_Test extends FilesBaseTest {
    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> files.assertIsRelative(someInfo(), null)).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_fail_if_actual_is_not_relative_path() {
        Mockito.when(actual.isAbsolute()).thenReturn(true);
        AssertionInfo info = TestData.someInfo();
        try {
            files.assertIsRelative(info, actual);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldBeRelativePath.shouldBeRelativePath(actual));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }

    @Test
    public void should_pass_if_actual_is_relative_path() {
        Mockito.when(actual.isAbsolute()).thenReturn(false);
        files.assertIsRelative(TestData.someInfo(), actual);
    }
}
