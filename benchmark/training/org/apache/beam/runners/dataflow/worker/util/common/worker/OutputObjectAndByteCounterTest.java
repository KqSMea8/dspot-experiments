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
package org.apache.beam.runners.dataflow.worker.util.common.worker;


import LongCounterMean.ZERO;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.runners.dataflow.worker.NameContextsForTests;
import org.apache.beam.runners.dataflow.worker.counters.Counter.CounterUpdateExtractor;
import org.apache.beam.runners.dataflow.worker.counters.CounterFactory.CounterMean;
import org.apache.beam.runners.dataflow.worker.counters.CounterSet;
import org.apache.beam.sdk.coders.CoderException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import static org.apache.beam.runners.dataflow.worker.util.common.worker.TestOutputReceiver.TestOutputCounter.getMeanByteCounterName;
import static org.apache.beam.runners.dataflow.worker.util.common.worker.TestOutputReceiver.TestOutputCounter.getObjectCounterName;


/**
 * Tests for {@link OutputObjectAndByteCounter}.
 */
@RunWith(JUnit4.class)
public class OutputObjectAndByteCounterTest {
    private final CounterSet counterSet = new CounterSet();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testUpdate() throws Exception {
        TestOutputReceiver.TestOutputCounter outputCounter = new TestOutputReceiver.TestOutputCounter(NameContextsForTests.nameContextForTest());
        outputCounter.update("hi");
        finishLazyUpdate("hi");
        outputCounter.update("bob");
        finishLazyUpdate("bob");
        CounterMean<Long> meanByteCount = getMeanByteCount().getAggregate();
        Assert.assertEquals(7, ((long) (meanByteCount.getAggregate())));
        Assert.assertEquals(2, meanByteCount.getCount());
    }

    @Test
    public void testIncorrectType() throws Exception {
        TestOutputReceiver.TestOutputCounter outputCounter = new TestOutputReceiver.TestOutputCounter(NameContextsForTests.nameContextForTest());
        thrown.expect(ClassCastException.class);
        outputCounter.update(5);
    }

    @Test
    public void testNullArgument() throws Exception {
        TestOutputReceiver.TestOutputCounter outputCounter = new TestOutputReceiver.TestOutputCounter(NameContextsForTests.nameContextForTest());
        thrown.expect(CoderException.class);
        update(null);
    }

    @Test
    public void testAddingCountersIntoCounterSet() throws Exception {
        new TestOutputReceiver.TestOutputCounter(counterSet, NameContextsForTests.nameContextForTest());
        CounterUpdateExtractor<?> updateExtractor = Mockito.mock(CounterUpdateExtractor.class);
        counterSet.extractUpdates(false, updateExtractor);
        Mockito.verify(updateExtractor).longMean(getMeanByteCounterName("output_name"), false, ZERO);
        Mockito.verify(updateExtractor).longSum(getObjectCounterName("output_name"), false, 0L);
        Mockito.verifyNoMoreInteractions(updateExtractor);
    }

    @Test
    public void testSimpleByteCount() throws Exception {
        OutputObjectAndByteCounter counter = makeCounter("byte_count", 1, 0);
        for (int i = 0; i < 10000; i++) {
            counter.update("foo");
        }
        Assert.assertEquals(40000L, ((long) (counter.getByteCount().getAggregate())));
    }

    @Test
    public void testSamplingByteCountFewElements() throws Exception {
        OutputObjectAndByteCounter counter = makeCounter("byte_count", 100, 0);
        for (int i = 0; i < 10; i++) {
            counter.update("foo");
        }
        Assert.assertEquals(40L, ((long) (counter.getByteCount().getAggregate())));
    }

    @Test
    public void testSamplingByteCount() throws Exception {
        List<Long> expected = Arrays.asList(4009645L, 3979997L, 4014060L);
        for (int n = 0; n < 3; n++) {
            OutputObjectAndByteCounter counter = makeCounter(("byte_count_" + n), 100, n);
            for (int i = 0; i < 1000000; i++) {
                counter.update("foo");
            }
            Assert.assertEquals(expected.get(n), counter.getByteCount().getAggregate());
        }
    }
}
