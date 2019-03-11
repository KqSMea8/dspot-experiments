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
package org.assertj.core.api.file;


import java.nio.charset.Charset;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.FileAssertBaseTest;
import org.junit.jupiter.api.Test;


/**
 * Test for <code>{@link FileAssert#usingCharset(Charset)}</code> when the provided charset is null.
 *
 * @author Olivier Michallat
 */
public class FileAssert_usingCharset_null_Test extends FileAssertBaseTest {
    @Override
    @Test
    public void should_have_internal_effects() {
        Assertions.assertThatNullPointerException().isThrownBy(() -> assertions.usingCharset(((Charset) (null)))).withMessage("The charset should not be null");
    }

    @Override
    @Test
    public void should_return_this() {
        // Disable this test since the call fails
    }
}
