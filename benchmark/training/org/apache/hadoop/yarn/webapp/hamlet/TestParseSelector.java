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
package org.apache.hadoop.yarn.webapp.hamlet;


import org.apache.hadoop.yarn.webapp.WebAppException;
import org.junit.Assert;
import org.junit.Test;


public class TestParseSelector {
    @Test
    public void testNormal() {
        String[] res = parseSelector("#id.class");
        Assert.assertEquals("id", res[S_ID]);
        Assert.assertEquals("class", res[S_CLASS]);
    }

    @Test
    public void testMultiClass() {
        String[] res = parseSelector("#id.class1.class2");
        Assert.assertEquals("id", res[S_ID]);
        Assert.assertEquals("class1 class2", res[S_CLASS]);
    }

    @Test
    public void testMissingId() {
        String[] res = parseSelector(".class");
        Assert.assertNull(res[S_ID]);
        Assert.assertEquals("class", res[S_CLASS]);
    }

    @Test
    public void testMissingClass() {
        String[] res = parseSelector("#id");
        Assert.assertEquals("id", res[S_ID]);
        Assert.assertNull(res[S_CLASS]);
    }

    @Test(expected = WebAppException.class)
    public void testMissingAll() {
        parseSelector("");
    }
}
