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
package org.apache.kafka.streams;


import StreamsConfig.OPTIMIZE;
import StreamsConfig.TOPOLOGY_OPTIMIZATION;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.errors.TopologyException;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.processor.internals.InternalTopologyBuilder;
import org.apache.kafka.streams.processor.internals.ProcessorTopology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.test.MockMapper;
import org.apache.kafka.test.MockPredicate;
import org.apache.kafka.test.MockProcessor;
import org.apache.kafka.test.MockProcessorSupplier;
import org.apache.kafka.test.MockValueJoiner;
import org.apache.kafka.test.StreamsTestUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;


public class StreamsBuilderTest {
    private final StreamsBuilder builder = new StreamsBuilder();

    private final Properties props = StreamsTestUtils.getStreamsConfig(Serdes.String(), Serdes.String());

    @Test
    public void shouldNotThrowNullPointerIfOptimizationsNotSpecified() {
        final Properties properties = new Properties();
        final StreamsBuilder builder = new StreamsBuilder();
        builder.build(properties);
    }

    @Test
    public void shouldAllowJoinUnmaterializedFilteredKTable() {
        final KTable<Bytes, String> filteredKTable = builder.<Bytes, String>table("table-topic").filter(MockPredicate.allGoodPredicate());
        builder.<Bytes, String>stream("stream-topic").join(filteredKTable, MockValueJoiner.TOSTRING_JOINER);
        builder.build();
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KSTREAM-JOIN-0000000005"), CoreMatchers.equalTo(Collections.singleton(topology.stateStores().get(0).name())));
        Assert.assertTrue(topology.processorConnectedStateStores("KTABLE-FILTER-0000000003").isEmpty());
    }

    @Test
    public void shouldAllowJoinMaterializedFilteredKTable() {
        final KTable<Bytes, String> filteredKTable = builder.<Bytes, String>table("table-topic").filter(MockPredicate.allGoodPredicate(), Materialized.as("store"));
        builder.<Bytes, String>stream("stream-topic").join(filteredKTable, MockValueJoiner.TOSTRING_JOINER);
        builder.build();
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KSTREAM-JOIN-0000000005"), CoreMatchers.equalTo(Collections.singleton("store")));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KTABLE-FILTER-0000000003"), CoreMatchers.equalTo(Collections.singleton("store")));
    }

    @Test
    public void shouldAllowJoinUnmaterializedMapValuedKTable() {
        final KTable<Bytes, String> mappedKTable = builder.<Bytes, String>table("table-topic").mapValues(MockMapper.noOpValueMapper());
        builder.<Bytes, String>stream("stream-topic").join(mappedKTable, MockValueJoiner.TOSTRING_JOINER);
        builder.build();
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KSTREAM-JOIN-0000000005"), CoreMatchers.equalTo(Collections.singleton(topology.stateStores().get(0).name())));
        Assert.assertTrue(topology.processorConnectedStateStores("KTABLE-MAPVALUES-0000000003").isEmpty());
    }

    @Test
    public void shouldAllowJoinMaterializedMapValuedKTable() {
        final KTable<Bytes, String> mappedKTable = builder.<Bytes, String>table("table-topic").mapValues(MockMapper.noOpValueMapper(), Materialized.as("store"));
        builder.<Bytes, String>stream("stream-topic").join(mappedKTable, MockValueJoiner.TOSTRING_JOINER);
        builder.build();
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KSTREAM-JOIN-0000000005"), CoreMatchers.equalTo(Collections.singleton("store")));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KTABLE-MAPVALUES-0000000003"), CoreMatchers.equalTo(Collections.singleton("store")));
    }

    @Test
    public void shouldAllowJoinUnmaterializedJoinedKTable() {
        final KTable<Bytes, String> table1 = builder.table("table-topic1");
        final KTable<Bytes, String> table2 = builder.table("table-topic2");
        builder.<Bytes, String>stream("stream-topic").join(table1.join(table2, MockValueJoiner.TOSTRING_JOINER), MockValueJoiner.TOSTRING_JOINER);
        builder.build();
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(2));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KSTREAM-JOIN-0000000010"), CoreMatchers.equalTo(Utils.mkSet(topology.stateStores().get(0).name(), topology.stateStores().get(1).name())));
        Assert.assertTrue(topology.processorConnectedStateStores("KTABLE-MERGE-0000000007").isEmpty());
    }

    @Test
    public void shouldAllowJoinMaterializedJoinedKTable() {
        final KTable<Bytes, String> table1 = builder.table("table-topic1");
        final KTable<Bytes, String> table2 = builder.table("table-topic2");
        builder.<Bytes, String>stream("stream-topic").join(table1.join(table2, MockValueJoiner.TOSTRING_JOINER, Materialized.as("store")), MockValueJoiner.TOSTRING_JOINER);
        builder.build();
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(3));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KSTREAM-JOIN-0000000010"), CoreMatchers.equalTo(Collections.singleton("store")));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KTABLE-MERGE-0000000007"), CoreMatchers.equalTo(Collections.singleton("store")));
    }

    @Test
    public void shouldAllowJoinMaterializedSourceKTable() {
        final KTable<Bytes, String> table = builder.table("table-topic");
        builder.<Bytes, String>stream("stream-topic").join(table, MockValueJoiner.TOSTRING_JOINER);
        builder.build();
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(1));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KTABLE-SOURCE-0000000002"), CoreMatchers.equalTo(Collections.singleton(topology.stateStores().get(0).name())));
        MatcherAssert.assertThat(topology.processorConnectedStateStores("KSTREAM-JOIN-0000000004"), CoreMatchers.equalTo(Collections.singleton(topology.stateStores().get(0).name())));
    }

    @Test
    public void shouldProcessingFromSinkTopic() {
        final KStream<String, String> source = builder.stream("topic-source");
        source.to("topic-sink");
        final MockProcessorSupplier<String, String> processorSupplier = new MockProcessorSupplier<>();
        source.process(processorSupplier);
        final ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory(new StringSerializer(), new StringSerializer());
        try (final TopologyTestDriver driver = new TopologyTestDriver(builder.build(), props)) {
            driver.pipeInput(recordFactory.create("topic-source", "A", "aa"));
        }
        // no exception was thrown
        Assert.assertEquals(Collections.singletonList("A:aa"), processorSupplier.theCapturedProcessor().processed);
    }

    @Test
    public void shouldProcessViaThroughTopic() {
        final KStream<String, String> source = builder.stream("topic-source");
        final KStream<String, String> through = source.through("topic-sink");
        final MockProcessorSupplier<String, String> sourceProcessorSupplier = new MockProcessorSupplier<>();
        source.process(sourceProcessorSupplier);
        final MockProcessorSupplier<String, String> throughProcessorSupplier = new MockProcessorSupplier<>();
        through.process(throughProcessorSupplier);
        final ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory(new StringSerializer(), new StringSerializer());
        try (final TopologyTestDriver driver = new TopologyTestDriver(builder.build(), props)) {
            driver.pipeInput(recordFactory.create("topic-source", "A", "aa"));
        }
        Assert.assertEquals(Collections.singletonList("A:aa"), sourceProcessorSupplier.theCapturedProcessor().processed);
        Assert.assertEquals(Collections.singletonList("A:aa"), throughProcessorSupplier.theCapturedProcessor().processed);
    }

    @Test
    public void shouldMergeStreams() {
        final String topic1 = "topic-1";
        final String topic2 = "topic-2";
        final KStream<String, String> source1 = builder.stream(topic1);
        final KStream<String, String> source2 = builder.stream(topic2);
        final KStream<String, String> merged = source1.merge(source2);
        final MockProcessorSupplier<String, String> processorSupplier = new MockProcessorSupplier<>();
        merged.process(processorSupplier);
        final ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory(new StringSerializer(), new StringSerializer());
        try (final TopologyTestDriver driver = new TopologyTestDriver(builder.build(), props)) {
            driver.pipeInput(recordFactory.create(topic1, "A", "aa"));
            driver.pipeInput(recordFactory.create(topic2, "B", "bb"));
            driver.pipeInput(recordFactory.create(topic2, "C", "cc"));
            driver.pipeInput(recordFactory.create(topic1, "D", "dd"));
        }
        Assert.assertEquals(Arrays.asList("A:aa", "B:bb", "C:cc", "D:dd"), processorSupplier.theCapturedProcessor().processed);
    }

    @Test
    public void shouldUseSerdesDefinedInMaterializedToConsumeTable() {
        final Map<Long, String> results = new HashMap<>();
        final String topic = "topic";
        final ForeachAction<Long, String> action = results::put;
        builder.table(topic, Materialized.<Long, String, KeyValueStore<Bytes, byte[]>>as("store").withKeySerde(Serdes.Long()).withValueSerde(Serdes.String())).toStream().foreach(action);
        final ConsumerRecordFactory<Long, String> recordFactory = new ConsumerRecordFactory(new LongSerializer(), new StringSerializer());
        try (final TopologyTestDriver driver = new TopologyTestDriver(builder.build(), props)) {
            driver.pipeInput(recordFactory.create(topic, 1L, "value1"));
            driver.pipeInput(recordFactory.create(topic, 2L, "value2"));
            final KeyValueStore<Long, String> store = driver.getKeyValueStore("store");
            MatcherAssert.assertThat(store.get(1L), CoreMatchers.equalTo("value1"));
            MatcherAssert.assertThat(store.get(2L), CoreMatchers.equalTo("value2"));
            MatcherAssert.assertThat(results.get(1L), CoreMatchers.equalTo("value1"));
            MatcherAssert.assertThat(results.get(2L), CoreMatchers.equalTo("value2"));
        }
    }

    @Test
    public void shouldUseSerdesDefinedInMaterializedToConsumeGlobalTable() {
        final String topic = "topic";
        builder.globalTable(topic, Materialized.<Long, String, KeyValueStore<Bytes, byte[]>>as("store").withKeySerde(Serdes.Long()).withValueSerde(Serdes.String()));
        final ConsumerRecordFactory<Long, String> recordFactory = new ConsumerRecordFactory(new LongSerializer(), new StringSerializer());
        try (final TopologyTestDriver driver = new TopologyTestDriver(builder.build(), props)) {
            driver.pipeInput(recordFactory.create(topic, 1L, "value1"));
            driver.pipeInput(recordFactory.create(topic, 2L, "value2"));
            final KeyValueStore<Long, String> store = driver.getKeyValueStore("store");
            MatcherAssert.assertThat(store.get(1L), CoreMatchers.equalTo("value1"));
            MatcherAssert.assertThat(store.get(2L), CoreMatchers.equalTo("value2"));
        }
    }

    @Test
    public void shouldNotMaterializeStoresIfNotRequired() {
        final String topic = "topic";
        builder.table(topic, Materialized.with(Serdes.Long(), Serdes.String()));
        final ProcessorTopology topology = builder.internalTopologyBuilder.rewriteTopology(new StreamsConfig(props)).build();
        MatcherAssert.assertThat(topology.stateStores().size(), CoreMatchers.equalTo(0));
    }

    @Test
    public void shouldReuseSourceTopicAsChangelogsWithOptimization20() {
        final String topic = "topic";
        builder.table(topic, Materialized.<Long, String, KeyValueStore<Bytes, byte[]>>as("store"));
        final Topology topology = builder.build();
        final Properties props = StreamsTestUtils.getStreamsConfig();
        props.put(TOPOLOGY_OPTIMIZATION, OPTIMIZE);
        final InternalTopologyBuilder internalTopologyBuilder = TopologyWrapper.getInternalTopologyBuilder(topology);
        internalTopologyBuilder.rewriteTopology(new StreamsConfig(props));
        MatcherAssert.assertThat(internalTopologyBuilder.build().storeToChangelogTopic(), CoreMatchers.equalTo(Collections.singletonMap("store", "topic")));
        MatcherAssert.assertThat(internalTopologyBuilder.getStateStores().keySet(), CoreMatchers.equalTo(Collections.singleton("store")));
        MatcherAssert.assertThat(internalTopologyBuilder.getStateStores().get("store").loggingEnabled(), CoreMatchers.equalTo(false));
        MatcherAssert.assertThat(internalTopologyBuilder.topicGroups().get(0).stateChangelogTopics.isEmpty(), CoreMatchers.equalTo(true));
    }

    @Test
    public void shouldNotReuseSourceTopicAsChangelogsByDefault() {
        final String topic = "topic";
        builder.table(topic, Materialized.<Long, String, KeyValueStore<Bytes, byte[]>>as("store"));
        final InternalTopologyBuilder internalTopologyBuilder = TopologyWrapper.getInternalTopologyBuilder(builder.build());
        internalTopologyBuilder.setApplicationId("appId");
        MatcherAssert.assertThat(internalTopologyBuilder.build().storeToChangelogTopic(), CoreMatchers.equalTo(Collections.singletonMap("store", "appId-store-changelog")));
        MatcherAssert.assertThat(internalTopologyBuilder.getStateStores().keySet(), CoreMatchers.equalTo(Collections.singleton("store")));
        MatcherAssert.assertThat(internalTopologyBuilder.getStateStores().get("store").loggingEnabled(), CoreMatchers.equalTo(true));
        MatcherAssert.assertThat(internalTopologyBuilder.topicGroups().get(0).stateChangelogTopics.keySet(), CoreMatchers.equalTo(Collections.singleton("appId-store-changelog")));
    }

    @Test(expected = TopologyException.class)
    public void shouldThrowExceptionWhenNoTopicPresent() {
        builder.stream(Collections.emptyList());
        builder.build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenTopicNamesAreNull() {
        builder.stream(Arrays.asList(null, null));
        builder.build();
    }
}
