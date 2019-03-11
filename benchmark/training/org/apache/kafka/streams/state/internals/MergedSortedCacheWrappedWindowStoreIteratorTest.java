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
package org.apache.kafka.streams.state.internals;


import ThreadCache.MemoryLRUCacheBytesIterator;
import java.util.ArrayList;
import java.util.List;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.common.utils.LogContext;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.internals.MockStreamsMetrics;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.StateSerdes;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;


public class MergedSortedCacheWrappedWindowStoreIteratorTest {
    private static final SegmentedCacheFunction SINGLE_SEGMENT_CACHE_FUNCTION = new SegmentedCacheFunction(null, (-1)) {
        @Override
        public long segmentId(final Bytes key) {
            return 0;
        }
    };

    private final List<KeyValue<Long, byte[]>> windowStoreKvPairs = new ArrayList<>();

    private final ThreadCache cache = new ThreadCache(new LogContext("testCache "), 1000000L, new MockStreamsMetrics(new Metrics()));

    private final String namespace = "0.0-one";

    private final StateSerdes<String, String> stateSerdes = new StateSerdes("foo", Serdes.String(), Serdes.String());

    @Test
    public void shouldIterateOverValueFromBothIterators() {
        final List<KeyValue<Long, byte[]>> expectedKvPairs = new ArrayList<>();
        for (long t = 0; t < 100; t += 20) {
            final byte[] v1Bytes = String.valueOf(t).getBytes();
            final KeyValue<Long, byte[]> v1 = KeyValue.pair(t, v1Bytes);
            windowStoreKvPairs.add(v1);
            expectedKvPairs.add(KeyValue.pair(t, v1Bytes));
            final Bytes keyBytes = WindowKeySchema.toStoreKeyBinary("a", (t + 10), 0, stateSerdes);
            final byte[] valBytes = String.valueOf((t + 10)).getBytes();
            expectedKvPairs.add(KeyValue.pair((t + 10), valBytes));
            cache.put(namespace, MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(keyBytes), new LRUCacheEntry(valBytes));
        }
        final Bytes fromBytes = WindowKeySchema.toStoreKeyBinary("a", 0, 0, stateSerdes);
        final Bytes toBytes = WindowKeySchema.toStoreKeyBinary("a", 100, 0, stateSerdes);
        final KeyValueIterator<Long, byte[]> storeIterator = new DelegatingPeekingKeyValueIterator("store", new org.apache.kafka.test.KeyValueIteratorStub(windowStoreKvPairs.iterator()));
        final ThreadCache.MemoryLRUCacheBytesIterator cacheIterator = cache.range(namespace, MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(fromBytes), MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(toBytes));
        final MergedSortedCacheWindowStoreIterator iterator = new MergedSortedCacheWindowStoreIterator(cacheIterator, storeIterator);
        int index = 0;
        while (iterator.hasNext()) {
            final KeyValue<Long, byte[]> next = iterator.next();
            final KeyValue<Long, byte[]> expected = expectedKvPairs.get((index++));
            Assert.assertArrayEquals(expected.value, next.value);
            Assert.assertEquals(expected.key, next.key);
        } 
        iterator.close();
    }

    @Test
    public void shouldPeekNextStoreKey() {
        windowStoreKvPairs.add(KeyValue.pair(10L, "a".getBytes()));
        cache.put(namespace, MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(WindowKeySchema.toStoreKeyBinary("a", 0, 0, stateSerdes)), new LRUCacheEntry("b".getBytes()));
        final Bytes fromBytes = WindowKeySchema.toStoreKeyBinary("a", 0, 0, stateSerdes);
        final Bytes toBytes = WindowKeySchema.toStoreKeyBinary("a", 100, 0, stateSerdes);
        final KeyValueIterator<Long, byte[]> storeIterator = new DelegatingPeekingKeyValueIterator("store", new org.apache.kafka.test.KeyValueIteratorStub(windowStoreKvPairs.iterator()));
        final ThreadCache.MemoryLRUCacheBytesIterator cacheIterator = cache.range(namespace, MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(fromBytes), MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(toBytes));
        final MergedSortedCacheWindowStoreIterator iterator = new MergedSortedCacheWindowStoreIterator(cacheIterator, storeIterator);
        MatcherAssert.assertThat(iterator.peekNextKey(), CoreMatchers.equalTo(0L));
        iterator.next();
        MatcherAssert.assertThat(iterator.peekNextKey(), CoreMatchers.equalTo(10L));
        iterator.close();
    }

    @Test
    public void shouldPeekNextCacheKey() {
        windowStoreKvPairs.add(KeyValue.pair(0L, "a".getBytes()));
        cache.put(namespace, MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(WindowKeySchema.toStoreKeyBinary("a", 10L, 0, stateSerdes)), new LRUCacheEntry("b".getBytes()));
        final Bytes fromBytes = WindowKeySchema.toStoreKeyBinary("a", 0, 0, stateSerdes);
        final Bytes toBytes = WindowKeySchema.toStoreKeyBinary("a", 100, 0, stateSerdes);
        final KeyValueIterator<Long, byte[]> storeIterator = new DelegatingPeekingKeyValueIterator("store", new org.apache.kafka.test.KeyValueIteratorStub(windowStoreKvPairs.iterator()));
        final ThreadCache.MemoryLRUCacheBytesIterator cacheIterator = cache.range(namespace, MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(fromBytes), MergedSortedCacheWrappedWindowStoreIteratorTest.SINGLE_SEGMENT_CACHE_FUNCTION.cacheKey(toBytes));
        final MergedSortedCacheWindowStoreIterator iterator = new MergedSortedCacheWindowStoreIterator(cacheIterator, storeIterator);
        MatcherAssert.assertThat(iterator.peekNextKey(), CoreMatchers.equalTo(0L));
        iterator.next();
        MatcherAssert.assertThat(iterator.peekNextKey(), CoreMatchers.equalTo(10L));
        iterator.close();
    }
}
