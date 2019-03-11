/**
 * Copyright 2016 KairosDB Authors
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.kairosdb.core.aggregator;


import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.kairosdb.core.DataPoint;
import org.kairosdb.core.datapoints.DoubleDataPoint;
import org.kairosdb.core.datapoints.DoubleDataPointFactoryImpl;
import org.kairosdb.core.datapoints.LegacyLongDataPoint;
import org.kairosdb.core.datapoints.LongDataPoint;
import org.kairosdb.core.datastore.DataPointGroup;
import org.kairosdb.core.datastore.TimeUnit;
import org.kairosdb.testing.ListDataPointGroup;


public class LastAggregatorTest {
    private LastAggregator aggregator;

    @Test(expected = NullPointerException.class)
    public void test_nullSet_invalid() {
        aggregator.aggregate(null);
    }

    @Test
    public void test_longValues() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new LongDataPoint(1, 10));
        group.addDataPoint(new LongDataPoint(1, 20));
        group.addDataPoint(new LongDataPoint(1, 3));
        group.addDataPoint(new LongDataPoint(2, 1));
        group.addDataPoint(new LongDataPoint(2, 3));
        group.addDataPoint(new LongDataPoint(2, 5));
        group.addDataPoint(new LongDataPoint(3, 25));
        DataPointGroup results = aggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(1L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(3L));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(2L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(5L));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(3L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(25L));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }

    @Test
    public void test_doubleValues() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new DoubleDataPoint(1, 10.0));
        group.addDataPoint(new DoubleDataPoint(1, 20.3));
        group.addDataPoint(new DoubleDataPoint(1, 3.0));
        group.addDataPoint(new DoubleDataPoint(2, 1.0));
        group.addDataPoint(new DoubleDataPoint(2, 3.2));
        group.addDataPoint(new DoubleDataPoint(2, 5.0));
        group.addDataPoint(new DoubleDataPoint(3, 25.1));
        DataPointGroup results = aggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(1L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo(3.0));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(2L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo(5.0));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(3L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo(25.1));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }

    @Test
    public void test_mixedTypeValues() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new DoubleDataPoint(1, 10.0));
        group.addDataPoint(new DoubleDataPoint(1, 20.3));
        group.addDataPoint(new LongDataPoint(1, 3));
        group.addDataPoint(new LongDataPoint(2, 1));
        group.addDataPoint(new DoubleDataPoint(2, 3.2));
        group.addDataPoint(new DoubleDataPoint(2, 5.0));
        group.addDataPoint(new DoubleDataPoint(3, 25.1));
        DataPointGroup results = aggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(1L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo(3.0));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(2L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo(5.0));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(3L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo(25.1));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }

    @Test
    public void test_withNegativeValues() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new DoubleDataPoint(1, (-10.0)));
        group.addDataPoint(new DoubleDataPoint(1, (-20.3)));
        group.addDataPoint(new LongDataPoint(1, (-3)));
        group.addDataPoint(new DoubleDataPoint(2, 1.0));
        group.addDataPoint(new DoubleDataPoint(2, (-3.2)));
        group.addDataPoint(new DoubleDataPoint(2, (-5.0)));
        group.addDataPoint(new DoubleDataPoint(3, (-25.1)));
        group.addDataPoint(new DoubleDataPoint(3, (-10.1)));
        DataPointGroup results = aggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(1L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo((-3.0)));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(2L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo((-5.0)));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(3L));
        Assert.assertThat(dataPoint.getDoubleValue(), CoreMatchers.equalTo((-10.1)));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }

    @Test
    public void test_legacyLongValues() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new LegacyLongDataPoint(1, 10));
        group.addDataPoint(new LegacyLongDataPoint(1, 20));
        group.addDataPoint(new LegacyLongDataPoint(1, 3));
        group.addDataPoint(new LegacyLongDataPoint(1, 1));
        group.addDataPoint(new LegacyLongDataPoint(1, 3));
        group.addDataPoint(new LegacyLongDataPoint(1, 5));
        group.addDataPoint(new LegacyLongDataPoint(1, 25));
        DataPointGroup results = aggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(1L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(25L));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }

    @Test
    public void test_alignStartOn() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new LongDataPoint(0, 11));
        group.addDataPoint(new LongDataPoint(1, 10));
        group.addDataPoint(new LongDataPoint(2, 20));
        group.addDataPoint(new LongDataPoint(3, 3));
        group.addDataPoint(new LongDataPoint(5, 1));
        group.addDataPoint(new LongDataPoint(6, 3));
        group.addDataPoint(new LongDataPoint(7, 5));
        group.addDataPoint(new LongDataPoint(8, 25));
        LastAggregator lastAggregator = new LastAggregator(new DoubleDataPointFactoryImpl());
        lastAggregator.setSampling(new Sampling(5, TimeUnit.MILLISECONDS));
        lastAggregator.setAlignStartTime(true);
        DataPointGroup results = lastAggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(0L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(3L));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(5L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(25L));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }

    @Test
    public void test_alignStartOff() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new LongDataPoint(0, 11));
        group.addDataPoint(new LongDataPoint(1, 10));
        group.addDataPoint(new LongDataPoint(2, 20));
        group.addDataPoint(new LongDataPoint(3, 3));
        group.addDataPoint(new LongDataPoint(5, 1));
        group.addDataPoint(new LongDataPoint(6, 3));
        group.addDataPoint(new LongDataPoint(7, 5));
        group.addDataPoint(new LongDataPoint(8, 25));
        LastAggregator lastAggregator = new LastAggregator(new DoubleDataPointFactoryImpl());
        lastAggregator.setSampling(new Sampling(5, TimeUnit.MILLISECONDS));
        lastAggregator.setAlignStartTime(false);
        DataPointGroup results = lastAggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(3L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(3L));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(8L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(25L));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }

    @Test
    public void test_alignEndOn() {
        ListDataPointGroup group = new ListDataPointGroup("group");
        group.addDataPoint(new LongDataPoint(0, 11));
        group.addDataPoint(new LongDataPoint(1, 10));
        group.addDataPoint(new LongDataPoint(2, 20));
        group.addDataPoint(new LongDataPoint(3, 3));
        group.addDataPoint(new LongDataPoint(5, 1));
        group.addDataPoint(new LongDataPoint(6, 3));
        group.addDataPoint(new LongDataPoint(7, 5));
        group.addDataPoint(new LongDataPoint(8, 25));
        LastAggregator lastAggregator = new LastAggregator(new DoubleDataPointFactoryImpl());
        lastAggregator.setSampling(new Sampling(5, TimeUnit.MILLISECONDS));
        lastAggregator.setAlignEndTime(true);
        DataPointGroup results = lastAggregator.aggregate(group);
        DataPoint dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(5L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(3L));
        dataPoint = results.next();
        Assert.assertThat(dataPoint.getTimestamp(), CoreMatchers.equalTo(10L));
        Assert.assertThat(dataPoint.getLongValue(), CoreMatchers.equalTo(25L));
        Assert.assertThat(results.hasNext(), CoreMatchers.equalTo(false));
    }
}
