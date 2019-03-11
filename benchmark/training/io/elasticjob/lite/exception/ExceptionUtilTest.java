/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */
package io.elasticjob.lite.exception;


import junit.framework.TestCase;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;


public final class ExceptionUtilTest {
    @Test
    public void assertTransformWithError() {
        TestCase.assertTrue(ExceptionUtil.transform(new Error("Error")).startsWith("java.lang.Error"));
    }

    @Test
    public void assertTransformWithException() {
        TestCase.assertTrue(ExceptionUtil.transform(new Exception("Exception")).startsWith("java.lang.Exception"));
    }

    @Test
    public void assertTransformWithNull() {
        Assert.assertThat(ExceptionUtil.transform(null), Is.is(""));
    }
}
