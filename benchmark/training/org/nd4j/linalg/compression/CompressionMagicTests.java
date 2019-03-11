/**
 * *****************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ****************************************************************************
 */
package org.nd4j.linalg.compression;


import DataType.FLOAT;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.BaseNd4jTest;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.factory.Nd4jBackend;


/**
 *
 *
 * @author raver119@gmail.com
 */
@RunWith(Parameterized.class)
public class CompressionMagicTests extends BaseNd4jTest {
    public CompressionMagicTests(Nd4jBackend backend) {
        super(backend);
    }

    @Test
    public void testMagicDecompression1() {
        INDArray array = Nd4j.linspace(1, 100, 2500, FLOAT);
        INDArray compressed = Nd4j.getCompressor().compress(array, "GZIP");
        Assert.assertTrue(compressed.isCompressed());
        compressed.muli(1.0);
        Assert.assertFalse(compressed.isCompressed());
        Assert.assertEquals(array, compressed);
    }

    @Test
    public void testMagicDecompression4() {
        INDArray array = Nd4j.linspace(1, 100, 2500, FLOAT);
        INDArray compressed = Nd4j.getCompressor().compress(array, "GZIP");
        for (int cnt = 0; cnt < (array.length()); cnt++) {
            float a = array.getFloat(cnt);
            float c = compressed.getFloat(cnt);
            Assert.assertEquals(a, c, 0.01F);
        }
    }

    @Test
    public void testDupSkipDecompression1() {
        INDArray array = Nd4j.linspace(1, 100, 2500, FLOAT);
        INDArray compressed = Nd4j.getCompressor().compress(array, "GZIP");
        INDArray newArray = compressed.dup();
        Assert.assertTrue(newArray.isCompressed());
        Nd4j.getCompressor().decompressi(compressed);
        Nd4j.getCompressor().decompressi(newArray);
        Assert.assertEquals(array, compressed);
        Assert.assertEquals(array, newArray);
    }

    @Test
    public void testDupSkipDecompression2() {
        INDArray array = Nd4j.linspace(1, 100, 2500, FLOAT);
        INDArray compressed = Nd4j.getCompressor().compress(array, "GZIP");
        INDArray newArray = compressed.dup('c');
        Assert.assertTrue(newArray.isCompressed());
        Nd4j.getCompressor().decompressi(compressed);
        Nd4j.getCompressor().decompressi(newArray);
        Assert.assertEquals(array, compressed);
        Assert.assertEquals(array, newArray);
    }

    @Test
    public void testDupSkipDecompression3() {
        INDArray array = Nd4j.linspace(1, 100, 2500, FLOAT);
        INDArray compressed = Nd4j.getCompressor().compress(array, "GZIP");
        INDArray newArray = compressed.dup('f');
        Assert.assertFalse(newArray.isCompressed());
        Nd4j.getCompressor().decompressi(compressed);
        // Nd4j.getCompressor().decompressi(newArray);
        Assert.assertEquals(array, compressed);
        Assert.assertEquals(array, newArray);
        Assert.assertEquals('f', newArray.ordering());
        Assert.assertEquals('c', compressed.ordering());
    }
}
