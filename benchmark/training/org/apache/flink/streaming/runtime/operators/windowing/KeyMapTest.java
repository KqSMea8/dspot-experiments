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
package org.apache.flink.streaming.runtime.operators.windowing;


import KeyMap.CapacityDescendingComparator.INSTANCE;
import KeyMap.TraversalEvaluator;
import java.util.HashMap;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for {@link KeyMap}.
 */
public class KeyMapTest {
    @Test
    public void testInitialSizeComputation() {
        try {
            KeyMap<String, String> map;
            map = new KeyMap();
            Assert.assertEquals(64, map.getCurrentTableCapacity());
            Assert.assertEquals(6, map.getLog2TableCapacity());
            Assert.assertEquals(24, map.getShift());
            Assert.assertEquals(48, map.getRehashThreshold());
            map = new KeyMap(0);
            Assert.assertEquals(64, map.getCurrentTableCapacity());
            Assert.assertEquals(6, map.getLog2TableCapacity());
            Assert.assertEquals(24, map.getShift());
            Assert.assertEquals(48, map.getRehashThreshold());
            map = new KeyMap(1);
            Assert.assertEquals(64, map.getCurrentTableCapacity());
            Assert.assertEquals(6, map.getLog2TableCapacity());
            Assert.assertEquals(24, map.getShift());
            Assert.assertEquals(48, map.getRehashThreshold());
            map = new KeyMap(9);
            Assert.assertEquals(64, map.getCurrentTableCapacity());
            Assert.assertEquals(6, map.getLog2TableCapacity());
            Assert.assertEquals(24, map.getShift());
            Assert.assertEquals(48, map.getRehashThreshold());
            map = new KeyMap(63);
            Assert.assertEquals(64, map.getCurrentTableCapacity());
            Assert.assertEquals(6, map.getLog2TableCapacity());
            Assert.assertEquals(24, map.getShift());
            Assert.assertEquals(48, map.getRehashThreshold());
            map = new KeyMap(64);
            Assert.assertEquals(128, map.getCurrentTableCapacity());
            Assert.assertEquals(7, map.getLog2TableCapacity());
            Assert.assertEquals(23, map.getShift());
            Assert.assertEquals(96, map.getRehashThreshold());
            map = new KeyMap(500);
            Assert.assertEquals(512, map.getCurrentTableCapacity());
            Assert.assertEquals(9, map.getLog2TableCapacity());
            Assert.assertEquals(21, map.getShift());
            Assert.assertEquals(384, map.getRehashThreshold());
            map = new KeyMap(127);
            Assert.assertEquals(128, map.getCurrentTableCapacity());
            Assert.assertEquals(7, map.getLog2TableCapacity());
            Assert.assertEquals(23, map.getShift());
            Assert.assertEquals(96, map.getRehashThreshold());
            // no negative number of elements
            try {
                new KeyMap((-1));
                Assert.fail("should fail with an exception");
            } catch (IllegalArgumentException e) {
                // expected
            }
            // check integer overflow
            try {
                map = new KeyMap(1701926178);
                final int maxCap = Integer.highestOneBit(Integer.MAX_VALUE);
                Assert.assertEquals(Integer.highestOneBit(Integer.MAX_VALUE), map.getCurrentTableCapacity());
                Assert.assertEquals(30, map.getLog2TableCapacity());
                Assert.assertEquals(0, map.getShift());
                Assert.assertEquals(((maxCap / 4) * 3), map.getRehashThreshold());
            } catch (OutOfMemoryError e) {
                // this may indeed happen in small test setups. we tolerate this in this test
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testPutAndGetRandom() {
        try {
            final KeyMap<Integer, Integer> map = new KeyMap();
            final Random rnd = new Random();
            final long seed = rnd.nextLong();
            final int numElements = 10000;
            final HashMap<Integer, Integer> groundTruth = new HashMap<>();
            rnd.setSeed(seed);
            for (int i = 0; i < numElements; i++) {
                Integer key = rnd.nextInt();
                Integer value = rnd.nextInt();
                if (rnd.nextBoolean()) {
                    groundTruth.put(key, value);
                    map.put(key, value);
                }
            }
            rnd.setSeed(seed);
            for (int i = 0; i < numElements; i++) {
                Integer key = rnd.nextInt();
                // skip these, evaluating it is tricky due to duplicates
                rnd.nextInt();
                rnd.nextBoolean();
                Integer expected = groundTruth.get(key);
                if (expected == null) {
                    Assert.assertNull(map.get(key));
                } else {
                    Integer contained = map.get(key);
                    Assert.assertNotNull(contained);
                    Assert.assertEquals(expected, contained);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testConjunctTraversal() {
        try {
            final Random rootRnd = new Random(654685486325439L);
            final int numMaps = 7;
            final int numKeys = 1000000;
            // ------ create a set of maps ------
            @SuppressWarnings("unchecked")
            final KeyMap<Integer, Integer>[] maps = ((KeyMap<Integer, Integer>[]) (new KeyMap<?, ?>[numMaps]));
            for (int i = 0; i < numMaps; i++) {
                maps[i] = new KeyMap();
            }
            // ------ prepare probabilities for maps ------
            final double[] probabilities = new double[numMaps];
            final double[] probabilitiesTemp = new double[numMaps];
            {
                probabilities[0] = 0.5;
                double remainingProb = 1.0 - (probabilities[0]);
                for (int i = 1; i < (numMaps - 1); i++) {
                    remainingProb /= 2;
                    probabilities[i] = remainingProb;
                }
                // compensate for rounding errors
                probabilities[(numMaps - 1)] = remainingProb;
            }
            // ------ generate random elements ------
            final long probSeed = rootRnd.nextLong();
            final long keySeed = rootRnd.nextLong();
            final Random probRnd = new Random(probSeed);
            final Random keyRnd = new Random(keySeed);
            final int maxStride = (Integer.MAX_VALUE) / numKeys;
            int totalNumElements = 0;
            int nextKeyValue = 1;
            for (int i = 0; i < numKeys; i++) {
                int numCopies = (nextKeyValue % 3) + 1;
                System.arraycopy(probabilities, 0, probabilitiesTemp, 0, numMaps);
                double totalProb = 1.0;
                for (int copy = 0; copy < numCopies; copy++) {
                    int pos = KeyMapTest.drawPosProportionally(probabilitiesTemp, totalProb, probRnd);
                    totalProb -= probabilitiesTemp[pos];
                    probabilitiesTemp[pos] = 0.0;
                    Integer boxed = nextKeyValue;
                    Integer previous = maps[pos].put(boxed, boxed);
                    Assert.assertNull("Test problem - test does not assign unique maps", previous);
                }
                totalNumElements += numCopies;
                nextKeyValue += (keyRnd.nextInt(maxStride)) + 1;
            }
            // check that all maps contain the total number of elements
            int numContained = 0;
            for (KeyMap<?, ?> map : maps) {
                numContained += map.size();
            }
            Assert.assertEquals(totalNumElements, numContained);
            // ------ check that all elements can be found in the maps ------
            keyRnd.setSeed(keySeed);
            numContained = 0;
            nextKeyValue = 1;
            for (int i = 0; i < numKeys; i++) {
                int numCopiesExpected = (nextKeyValue % 3) + 1;
                int numCopiesContained = 0;
                for (KeyMap<Integer, Integer> map : maps) {
                    Integer val = map.get(nextKeyValue);
                    if (val != null) {
                        Assert.assertEquals(nextKeyValue, val.intValue());
                        numCopiesContained++;
                    }
                }
                Assert.assertEquals(numCopiesExpected, numCopiesContained);
                numContained += numCopiesContained;
                nextKeyValue += (keyRnd.nextInt(maxStride)) + 1;
            }
            Assert.assertEquals(totalNumElements, numContained);
            // ------ make a traversal over all keys and validate the keys in the traversal ------
            final int[] keysStartedAndFinished = new int[]{ 0, 0 };
            TraversalEvaluator<Integer, Integer> traversal = new TraversalEvaluator<Integer, Integer>() {
                private int key;

                private int valueCount;

                @Override
                public void startNewKey(Integer key) {
                    this.key = key;
                    this.valueCount = 0;
                    (keysStartedAndFinished[0])++;
                }

                @Override
                public void nextValue(Integer value) {
                    assertEquals(this.key, value.intValue());
                    (this.valueCount)++;
                }

                @Override
                public void keyDone() {
                    int expected = ((key) % 3) + 1;
                    if (expected != (valueCount)) {
                        fail(((((("Wrong count for key " + (key)) + " ; expected=") + expected) + " , count=") + (valueCount)));
                    }
                    (keysStartedAndFinished[1])++;
                }
            };
            KeyMap.traverseMaps(KeyMapTest.shuffleArray(maps, rootRnd), traversal, 17);
            Assert.assertEquals(numKeys, keysStartedAndFinished[0]);
            Assert.assertEquals(numKeys, keysStartedAndFinished[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSizeComparator() {
        try {
            KeyMap<String, String> map1 = new KeyMap(5);
            KeyMap<String, String> map2 = new KeyMap(80);
            Assert.assertTrue(((map1.getCurrentTableCapacity()) < (map2.getCurrentTableCapacity())));
            Assert.assertTrue(((INSTANCE.compare(map1, map1)) == 0));
            Assert.assertTrue(((INSTANCE.compare(map2, map2)) == 0));
            Assert.assertTrue(((INSTANCE.compare(map1, map2)) > 0));
            Assert.assertTrue(((INSTANCE.compare(map2, map1)) < 0));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
