/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.druid.query.aggregation.distinctcount;


import Granularities.SECOND;
import OrderByColumnSpec.Direction;
import QueryRunnerTestHelper.allGran;
import QueryRunnerTestHelper.dataSource;
import QueryRunnerTestHelper.fullOnIntervalSpec;
import QueryRunnerTestHelper.rowsCount;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.druid.data.input.MapBasedInputRow;
import org.apache.druid.data.input.Row;
import org.apache.druid.java.util.common.io.Closer;
import org.apache.druid.query.aggregation.CountAggregatorFactory;
import org.apache.druid.query.dimension.DefaultDimensionSpec;
import org.apache.druid.query.groupby.GroupByQuery;
import org.apache.druid.query.groupby.GroupByQueryRunnerFactory;
import org.apache.druid.query.groupby.GroupByQueryRunnerTestHelper;
import org.apache.druid.segment.Segment;
import org.apache.druid.segment.TestHelper;
import org.apache.druid.segment.incremental.IncrementalIndex;
import org.apache.druid.segment.incremental.IncrementalIndexSchema;
import org.junit.Test;


public class DistinctCountGroupByQueryTest {
    private GroupByQueryRunnerFactory factory;

    private Closer resourceCloser;

    @Test
    public void testGroupByWithDistinctCountAgg() throws Exception {
        IncrementalIndex index = new IncrementalIndex.Builder().setIndexSchema(new IncrementalIndexSchema.Builder().withQueryGranularity(SECOND).withMetrics(new CountAggregatorFactory("cnt")).build()).setConcurrentEventAdd(true).setMaxRowCount(1000).buildOnheap();
        String visitor_id = "visitor_id";
        String client_type = "client_type";
        long timestamp = System.currentTimeMillis();
        index.add(new MapBasedInputRow(timestamp, Lists.newArrayList(visitor_id, client_type), ImmutableMap.of(visitor_id, "0", client_type, "iphone")));
        index.add(new MapBasedInputRow((timestamp + 1), Lists.newArrayList(visitor_id, client_type), ImmutableMap.of(visitor_id, "1", client_type, "iphone")));
        index.add(new MapBasedInputRow((timestamp + 2), Lists.newArrayList(visitor_id, client_type), ImmutableMap.of(visitor_id, "2", client_type, "android")));
        GroupByQuery query = new GroupByQuery.Builder().setDataSource(dataSource).setGranularity(allGran).setDimensions(new DefaultDimensionSpec(client_type, client_type)).setInterval(fullOnIntervalSpec).setLimitSpec(new org.apache.druid.query.groupby.orderby.DefaultLimitSpec(Collections.singletonList(new org.apache.druid.query.groupby.orderby.OrderByColumnSpec(client_type, Direction.DESCENDING)), 10)).setAggregatorSpecs(rowsCount, new DistinctCountAggregatorFactory("UV", visitor_id, null)).build();
        final Segment incrementalIndexSegment = new org.apache.druid.segment.IncrementalIndexSegment(index, null);
        Iterable<Row> results = GroupByQueryRunnerTestHelper.runQuery(factory, factory.createRunner(incrementalIndexSegment), query);
        List<Row> expectedResults = Arrays.asList(GroupByQueryRunnerTestHelper.createExpectedRow("1970-01-01T00:00:00.000Z", client_type, "iphone", "UV", 2L, "rows", 2L), GroupByQueryRunnerTestHelper.createExpectedRow("1970-01-01T00:00:00.000Z", client_type, "android", "UV", 1L, "rows", 1L));
        TestHelper.assertExpectedObjects(expectedResults, results, "distinct-count");
    }
}
