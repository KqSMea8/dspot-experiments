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
package com.twitter.distributedlog.service.streamset;


import org.junit.Assert;
import org.junit.Test;


public class TestIdentityStreamPartitionConverter {
    @Test(timeout = 20000)
    public void testIdentityConverter() {
        String streamName = "test-identity-converter";
        IdentityStreamPartitionConverter converter = new IdentityStreamPartitionConverter();
        Partition p0 = converter.convert(streamName);
        Assert.assertEquals(new Partition(streamName, 0), p0);
        Partition p1 = converter.convert(streamName);
        Assert.assertTrue((p0 == p1));
    }
}
