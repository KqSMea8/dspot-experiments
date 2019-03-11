/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.trevni;


import java.util.Arrays;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;


public class TestInputBytes {
    private static final int SIZE = 1000;

    private static final int COUNT = 100;

    @Test
    public void testRandomReads() throws Exception {
        Random random = new Random(19820210);
        int length = (random.nextInt(TestInputBytes.SIZE)) + 1;
        byte[] data = new byte[length];
        random.nextBytes(data);
        Input in = new InputBytes(data);
        for (int i = 0; i < (TestInputBytes.COUNT); i++) {
            int p = random.nextInt(length);
            int l = Math.min(random.nextInt(((TestInputBytes.SIZE) / 10)), (length - p));
            byte[] buffer = new byte[l];
            in.read(p, buffer, 0, l);
            Assert.assertArrayEquals(Arrays.copyOfRange(data, p, (p + l)), buffer);
        }
    }
}
