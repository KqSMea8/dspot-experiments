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


import org.assertj.core.api.Assertions;
import org.assertj.core.description.TextDescription;
import org.assertj.core.presentation.StandardRepresentation;
import org.junit.jupiter.api.Test;


/**
 * Tests for <code>{@link ShouldBeLowerCase#create(org.assertj.core.description.Description, org.assertj.core.presentation.Representation)}</code>.
 *
 * @author Alex Ruiz
 */
public class ShouldBeLowerCase_create_Test {
    @Test
    public void should_create_error_message_for_character() {
        String message = ShouldBeLowerCase.shouldBeLowerCase('A').create(new TextDescription("Test"), StandardRepresentation.STANDARD_REPRESENTATION);
        Assertions.assertThat(message).isEqualTo(String.format("[Test] %nExpecting <'A'> to be a lowercase"));
    }

    @Test
    public void should_create_error_message_for_string() {
        String message = ShouldBeLowerCase.shouldBeLowerCase("ABC").create(new TextDescription("Test"), StandardRepresentation.STANDARD_REPRESENTATION);
        Assertions.assertThat(message).isEqualTo(String.format("[Test] %nExpecting <\"ABC\"> to be a lowercase"));
    }
}
