/**
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.util;


import org.junit.Assert;
import org.junit.Test;


public class AttributeKeyTest {
    @Test
    public void testExists() {
        String name = "test";
        Assert.assertFalse(AttributeKey.exists(name));
        AttributeKey<String> attr = AttributeKey.valueOf(name);
        Assert.assertTrue(AttributeKey.exists(name));
        Assert.assertNotNull(attr);
    }

    @Test
    public void testValueOf() {
        String name = "test1";
        Assert.assertFalse(AttributeKey.exists(name));
        AttributeKey<String> attr = AttributeKey.valueOf(name);
        AttributeKey<String> attr2 = AttributeKey.valueOf(name);
        Assert.assertSame(attr, attr2);
    }

    @Test
    public void testNewInstance() {
        String name = "test2";
        Assert.assertFalse(AttributeKey.exists(name));
        AttributeKey<String> attr = AttributeKey.newInstance(name);
        Assert.assertTrue(AttributeKey.exists(name));
        Assert.assertNotNull(attr);
        try {
            AttributeKey.<String>newInstance(name);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
