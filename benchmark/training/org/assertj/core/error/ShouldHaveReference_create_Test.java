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
package org.assertj.core.error;


import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicStampedReference;
import org.assertj.core.api.Assertions;
import org.assertj.core.internal.TestDescription;
import org.junit.jupiter.api.Test;


public class ShouldHaveReference_create_Test {
    private static final TestDescription TEST_DESCRIPTION = new TestDescription("TEST");

    @Test
    public void should_create_error_message_for_AtomicMarkableReference() {
        // GIVEN
        AtomicMarkableReference<String> actual = new AtomicMarkableReference<>("foo", true);
        // WHEN
        String message = ShouldHaveReference.shouldHaveReference(actual, actual.getReference(), "bar").create(ShouldHaveReference_create_Test.TEST_DESCRIPTION, CONFIGURATION_PROVIDER.representation());
        // THEN
        Assertions.assertThat(message).isEqualTo(String.format(("[TEST] %n" + ((((("Expecting%n" + "  <AtomicMarkableReference[marked=true, reference=\"foo\"]>%n") + "to have reference:%n") + "  <\"bar\">%n") + "but had:%n") + "  <\"foo\">"))));
    }

    @Test
    public void should_create_error_message_for_AtomicStampedReference() {
        // GIVEN
        AtomicStampedReference<String> actual = new AtomicStampedReference<>("foo", 123);
        // WHEN
        String message = ShouldHaveReference.shouldHaveReference(actual, actual.getReference(), "bar").create(ShouldHaveReference_create_Test.TEST_DESCRIPTION, CONFIGURATION_PROVIDER.representation());
        // THEN
        Assertions.assertThat(message).isEqualTo(String.format(("[TEST] %n" + ((((("Expecting%n" + "  <AtomicStampedReference[stamp=123, reference=\"foo\"]>%n") + "to have reference:%n") + "  <\"bar\">%n") + "but had:%n") + "  <\"foo\">"))));
    }
}
