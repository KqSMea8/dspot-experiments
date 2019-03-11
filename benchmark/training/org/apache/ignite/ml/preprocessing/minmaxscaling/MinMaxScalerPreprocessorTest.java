/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.ml.preprocessing.minmaxscaling;


import org.apache.ignite.ml.math.primitives.vector.Vector;
import org.apache.ignite.ml.math.primitives.vector.VectorUtils;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link MinMaxScalerPreprocessor}.
 */
public class MinMaxScalerPreprocessorTest {
    /**
     * Tests {@code apply()} method.
     */
    @Test
    public void testApply() {
        double[][] data = new double[][]{ new double[]{ 2.0, 4.0, 1.0 }, new double[]{ 1.0, 8.0, 22.0 }, new double[]{ 4.0, 10.0, 100.0 }, new double[]{ 0.0, 22.0, 300.0 } };
        MinMaxScalerPreprocessor<Integer, Vector> preprocessor = new MinMaxScalerPreprocessor(new double[]{ 0, 4, 1 }, new double[]{ 4, 22, 300 }, ( k, v) -> v);
        double[][] standardData = new double[][]{ new double[]{ 2.0 / 4, (4.0 - 4.0) / 18.0, 0.0 }, new double[]{ 1.0 / 4, (8.0 - 4.0) / 18.0, (22.0 - 1.0) / 299.0 }, new double[]{ 1.0, (10.0 - 4.0) / 18.0, (100.0 - 1.0) / 299.0 }, new double[]{ 0.0, (22.0 - 4.0) / 18.0, (300.0 - 1.0) / 299.0 } };
        for (int i = 0; i < (data.length); i++)
            Assert.assertArrayEquals(standardData[i], preprocessor.apply(i, VectorUtils.of(data[i])).asArray(), 1.0E-8);

    }

    /**
     * Test {@code apply()} method with division by zero.
     */
    @Test
    public void testApplyDivisionByZero() {
        double[][] data = new double[][]{ new double[]{ 1.0 }, new double[]{ 1.0 }, new double[]{ 1.0 }, new double[]{ 1.0 } };
        MinMaxScalerPreprocessor<Integer, Vector> preprocessor = new MinMaxScalerPreprocessor(new double[]{ 1.0 }, new double[]{ 1.0 }, ( k, v) -> v);
        double[][] standardData = new double[][]{ new double[]{ 0.0 }, new double[]{ 0.0 }, new double[]{ 0.0 }, new double[]{ 0.0 } };
        for (int i = 0; i < (data.length); i++)
            Assert.assertArrayEquals(standardData[i], preprocessor.apply(i, VectorUtils.of(data[i])).asArray(), 1.0E-8);

    }
}
