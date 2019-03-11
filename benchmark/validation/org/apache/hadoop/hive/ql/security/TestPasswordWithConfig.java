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
package org.apache.hadoop.hive.ql.security;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.shims.ShimLoader;
import org.junit.Assert;
import org.junit.Test;


public class TestPasswordWithConfig {
    @Test
    public void testPassword() throws Exception {
        String key1 = "key1";
        String key2 = "key2";
        String val1 = "value1";
        Configuration conf = new Configuration();
        conf.set(key1, val1);
        Assert.assertEquals("key1 should exist in config", val1, ShimLoader.getHadoopShims().getPassword(conf, key1));
        Assert.assertNull("key2 should not exist in config", ShimLoader.getHadoopShims().getPassword(conf, key2));
    }
}
