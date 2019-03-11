/**
 * Copyright 2018-2019 Crown Copyright
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
package uk.gov.gchq.gaffer.sketches.datasketches.quantiles.binaryoperator;


import com.yahoo.sketches.kll.KllFloatsSketch;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.gchq.koryphe.binaryoperator.BinaryOperatorTest;


public class KllFloatsSketchAggregatorTest extends BinaryOperatorTest {
    private static final double DELTA = 0.01;

    private KllFloatsSketch sketch1;

    private KllFloatsSketch sketch2;

    @Test
    public void testAggregate() {
        final KllFloatsSketchAggregator sketchAggregator = new KllFloatsSketchAggregator();
        KllFloatsSketch currentState = sketch1;
        Assert.assertEquals(3L, currentState.getN());
        Assert.assertEquals(2.0, currentState.getQuantile(0.5), KllFloatsSketchAggregatorTest.DELTA);
        currentState = sketchAggregator.apply(currentState, sketch2);
        Assert.assertEquals(7L, currentState.getN());
        Assert.assertEquals(4.0, currentState.getQuantile(0.5), KllFloatsSketchAggregatorTest.DELTA);
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(new KllFloatsSketchAggregator(), new KllFloatsSketchAggregator());
    }
}
