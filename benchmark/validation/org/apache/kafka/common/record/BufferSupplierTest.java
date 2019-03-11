/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.common.record;


import BufferSupplier.GrowableBufferSupplier;
import java.nio.ByteBuffer;
import org.junit.Assert;
import org.junit.Test;


public class BufferSupplierTest {
    @Test
    public void testGrowableBuffer() {
        BufferSupplier.GrowableBufferSupplier supplier = new BufferSupplier.GrowableBufferSupplier();
        ByteBuffer buffer = supplier.get(1024);
        Assert.assertEquals(0, buffer.position());
        Assert.assertEquals(1024, buffer.capacity());
        supplier.release(buffer);
        ByteBuffer cached = supplier.get(512);
        Assert.assertEquals(0, cached.position());
        Assert.assertSame(buffer, cached);
        ByteBuffer increased = supplier.get(2048);
        Assert.assertEquals(2048, increased.capacity());
        Assert.assertEquals(0, increased.position());
    }
}
