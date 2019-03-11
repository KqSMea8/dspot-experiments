/**
 * Copyright 2017-2019 Crown Copyright
 *
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
 */
package uk.gov.gchq.gaffer.sketches.datasketches.cardinality.serialisation;


import com.yahoo.sketches.hll.Union;
import org.junit.Assert;
import org.junit.Test;


public class HllUnionSerialiserTest {
    private static final HllUnionSerialiser SERIALISER = new HllUnionSerialiser();

    private static final double DELTA = 1.0E-7;

    @Test
    public void testSerialiseAndDeserialise() {
        final Union sketch = new Union(15);
        sketch.update("A");
        sketch.update("B");
        sketch.update("C");
        testSerialiser(sketch);
        final Union emptySketch = new Union(15);
        testSerialiser(emptySketch);
    }

    @Test
    public void testCanHandleUnion() {
        Assert.assertTrue(HllUnionSerialiserTest.SERIALISER.canHandle(Union.class));
        Assert.assertFalse(HllUnionSerialiserTest.SERIALISER.canHandle(String.class));
    }
}
