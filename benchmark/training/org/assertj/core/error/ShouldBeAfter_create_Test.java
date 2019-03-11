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
 * Tests for <code>{@link ShouldBeAfter#create(Description, org.assertj.core.presentation.Representation)}</code>.
 *
 * @author Joel Costigliola
 */
public class ShouldBeAfter_create_Test {
    private ErrorMessageFactory factory;

    @Test
    public void should_create_error_message() {
        String message = factory.create(new TextDescription("Test"), new StandardRepresentation());
        Assertions.assertThat(message).isEqualTo(String.format(("[Test] %n" + ((("Expecting:%n" + "  <2011-01-01T00:00:00.000>%n") + "to be strictly after:%n") + "  <2012-01-01T00:00:00.000>"))));
    }
}
