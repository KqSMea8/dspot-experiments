/**
 * Copyright 2017 LinkedIn Corp. Licensed under the BSD 2-Clause License (the "License"). See License in the project root for license information.
 */
package com.linkedin.kafka.cruisecontrol.monitor.sampling;


import Resource.CPU;
import Resource.DISK;
import Resource.NW_IN;
import Resource.NW_OUT;
import com.linkedin.cruisecontrol.metricdef.MetricDef;
import com.linkedin.kafka.cruisecontrol.metricsreporter.exception.UnknownVersionException;
import com.linkedin.kafka.cruisecontrol.monitor.metricdefinition.KafkaMetricDef;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;


/**
 * The unit test for {@link PartitionMetricSample}
 */
public class PartitionMetricSampleTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    public void testRecordAfterClose() {
        PartitionMetricSample sample = new PartitionMetricSample(0, new TopicPartition("topic", 0));
        sample.close(0);
        try {
            sample.record(KafkaMetricDef.commonMetricDefInfo(DISK_USAGE), 0.0);
            Assert.fail("Should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            // let it go.
        }
    }

    @Test
    public void testRecordSameResourceMetricAgain() {
        PartitionMetricSample sample = new PartitionMetricSample(0, new TopicPartition("topic", 0));
        sample.record(KafkaMetricDef.commonMetricDefInfo(DISK_USAGE), 0);
        try {
            sample.record(KafkaMetricDef.commonMetricDefInfo(DISK_USAGE), 0.0);
            Assert.fail("Should throw IllegalStateException");
        } catch (IllegalStateException ise) {
            // let it go.
        }
    }

    @Test
    public void testSerde() throws UnknownVersionException {
        MetricDef metricDef = KafkaMetricDef.commonMetricDef();
        PartitionMetricSample sample = new PartitionMetricSample(0, new TopicPartition("topic", 0));
        int i = 0;
        for (com.linkedin.kafka.cruisecontrol.common.Resource r : com.linkedin.kafka.cruisecontrol.common.Resource.cachedValues()) {
            sample.record(KafkaMetricDef.resourceToMetricInfo(r).get(0), i);
            i++;
        }
        sample.record(metricDef.metricInfo(PRODUCE_RATE.name()), ((double) (i++)));
        sample.record(metricDef.metricInfo(FETCH_RATE.name()), ((double) (i++)));
        sample.record(metricDef.metricInfo(MESSAGE_IN_RATE.name()), ((double) (i++)));
        sample.record(metricDef.metricInfo(REPLICATION_BYTES_IN_RATE.name()), ((double) (i++)));
        sample.record(metricDef.metricInfo(REPLICATION_BYTES_OUT_RATE.name()), ((double) (i)));
        sample.close(10);
        byte[] bytes = sample.toBytes();
        PartitionMetricSample deserializedSample = PartitionMetricSample.fromBytes(bytes);
        Assert.assertEquals(sample.brokerId(), deserializedSample.brokerId());
        Assert.assertEquals(sample.entity().tp(), deserializedSample.entity().tp());
        Assert.assertEquals(sample.metricValue(KafkaMetricDef.resourceToMetricIds(CPU).get(0)), deserializedSample.metricValue(KafkaMetricDef.resourceToMetricIds(CPU).get(0)));
        Assert.assertEquals(sample.metricValue(KafkaMetricDef.resourceToMetricIds(DISK).get(0)), deserializedSample.metricValue(KafkaMetricDef.resourceToMetricIds(DISK).get(0)));
        Assert.assertEquals(sample.metricValue(KafkaMetricDef.resourceToMetricIds(NW_IN).get(0)), deserializedSample.metricValue(KafkaMetricDef.resourceToMetricIds(NW_IN).get(0)));
        Assert.assertEquals(sample.metricValue(KafkaMetricDef.resourceToMetricIds(NW_OUT).get(0)), deserializedSample.metricValue(KafkaMetricDef.resourceToMetricIds(NW_OUT).get(0)));
        Assert.assertEquals(sample.metricValue(metricDef.metricInfo(PRODUCE_RATE.name()).id()), deserializedSample.metricValue(metricDef.metricInfo(PRODUCE_RATE.name()).id()), PartitionMetricSampleTest.EPSILON);
        Assert.assertEquals(sample.metricValue(metricDef.metricInfo(FETCH_RATE.name()).id()), deserializedSample.metricValue(metricDef.metricInfo(FETCH_RATE.name()).id()), PartitionMetricSampleTest.EPSILON);
        Assert.assertEquals(sample.metricValue(metricDef.metricInfo(MESSAGE_IN_RATE.name()).id()), deserializedSample.metricValue(metricDef.metricInfo(MESSAGE_IN_RATE.name()).id()), PartitionMetricSampleTest.EPSILON);
        Assert.assertEquals(sample.metricValue(metricDef.metricInfo(REPLICATION_BYTES_IN_RATE.name()).id()), deserializedSample.metricValue(metricDef.metricInfo(REPLICATION_BYTES_IN_RATE.name()).id()), PartitionMetricSampleTest.EPSILON);
        Assert.assertEquals(sample.metricValue(metricDef.metricInfo(REPLICATION_BYTES_OUT_RATE.name()).id()), deserializedSample.metricValue(metricDef.metricInfo(REPLICATION_BYTES_OUT_RATE.name()).id()), PartitionMetricSampleTest.EPSILON);
        Assert.assertEquals(sample.sampleTime(), deserializedSample.sampleTime());
    }
}
