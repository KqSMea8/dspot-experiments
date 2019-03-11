/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.util;


import org.apache.flink.core.testutils.CommonTestUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for the {@link SerializedValue}.
 */
public class SerializedValueTest {
    @Test
    public void testSimpleValue() {
        try {
            final String value = "teststring";
            SerializedValue<String> v = new SerializedValue(value);
            SerializedValue<String> copy = CommonTestUtils.createCopySerializable(v);
            Assert.assertEquals(value, v.deserializeValue(getClass().getClassLoader()));
            Assert.assertEquals(value, copy.deserializeValue(getClass().getClassLoader()));
            Assert.assertEquals(v, copy);
            Assert.assertEquals(v.hashCode(), copy.hashCode());
            Assert.assertNotNull(v.toString());
            Assert.assertNotNull(copy.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNullValue() {
        try {
            SerializedValue<Object> v = new SerializedValue(null);
            SerializedValue<Object> copy = CommonTestUtils.createCopySerializable(v);
            Assert.assertNull(copy.deserializeValue(getClass().getClassLoader()));
            Assert.assertEquals(v, copy);
            Assert.assertEquals(v.hashCode(), copy.hashCode());
            Assert.assertEquals(v.toString(), copy.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
