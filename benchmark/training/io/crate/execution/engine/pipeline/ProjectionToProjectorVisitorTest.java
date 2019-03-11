/**
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */
package io.crate.execution.engine.pipeline;


import DataTypes.LONG;
import EqOperator.NAME;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.google.common.collect.ImmutableList;
import io.crate.breaker.RamAccountingContext;
import io.crate.data.BatchIterator;
import io.crate.data.Bucket;
import io.crate.data.CollectionBucket;
import io.crate.data.InMemoryBatchIterator;
import io.crate.data.Projector;
import io.crate.data.Row;
import io.crate.execution.dsl.projection.AggregationProjection;
import io.crate.execution.dsl.projection.FilterProjection;
import io.crate.execution.dsl.projection.GroupProjection;
import io.crate.execution.dsl.projection.OrderedTopNProjection;
import io.crate.execution.dsl.projection.TopNProjection;
import io.crate.execution.engine.aggregation.AggregationPipe;
import io.crate.execution.engine.aggregation.GroupingProjector;
import io.crate.execution.engine.sort.SortingProjector;
import io.crate.execution.engine.sort.SortingTopNProjector;
import io.crate.expression.operator.EqOperator;
import io.crate.expression.symbol.AggregateMode;
import io.crate.expression.symbol.Aggregation;
import io.crate.expression.symbol.Function;
import io.crate.expression.symbol.InputColumn;
import io.crate.expression.symbol.Literal;
import io.crate.expression.symbol.Symbol;
import io.crate.metadata.CoordinatorTxnCtx;
import io.crate.metadata.FunctionInfo;
import io.crate.metadata.Functions;
import io.crate.metadata.RowGranularity;
import io.crate.metadata.SearchPath;
import io.crate.metadata.TransactionContext;
import io.crate.test.integration.CrateUnitTest;
import io.crate.testing.TestingBatchIterators;
import io.crate.testing.TestingHelpers;
import io.crate.testing.TestingRowConsumer;
import io.crate.types.DataTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.elasticsearch.common.breaker.CircuitBreaker;
import org.elasticsearch.threadpool.ThreadPool;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Test;

import static TopN.NO_LIMIT;
import static TopN.NO_OFFSET;


public class ProjectionToProjectorVisitorTest extends CrateUnitTest {
    private static final RamAccountingContext RAM_ACCOUNTING_CONTEXT = new RamAccountingContext("dummy", new org.elasticsearch.common.breaker.NoopCircuitBreaker(CircuitBreaker.FIELDDATA));

    private ProjectionToProjectorVisitor visitor;

    private FunctionInfo countInfo;

    private FunctionInfo avgInfo;

    private Functions functions;

    private TransactionContext txnCtx = CoordinatorTxnCtx.systemTransactionContext();

    private ThreadPool threadPool;

    @Test
    public void testSimpleTopNProjection() throws Exception {
        TopNProjection projection = new TopNProjection(10, 2, Collections.singletonList(LONG));
        Projector projector = visitor.create(projection, txnCtx, ProjectionToProjectorVisitorTest.RAM_ACCOUNTING_CONTEXT, UUID.randomUUID());
        assertThat(projector, Matchers.instanceOf(SimpleTopNProjector.class));
        TestingRowConsumer consumer = new TestingRowConsumer();
        consumer.accept(projector.apply(TestingBatchIterators.range(0, 20)), null);
        List<Object[]> result = consumer.getResult();
        assertThat(result.size(), Is.is(10));
        assertThat(result.get(0), Is.is(new Object[]{ 2 }));
    }

    @Test
    public void testSortingTopNProjection() throws Exception {
        List<Symbol> outputs = Arrays.asList(Literal.of("foo"), new InputColumn(0), new InputColumn(1));
        OrderedTopNProjection projection = new OrderedTopNProjection(10, 0, outputs, Arrays.asList(new InputColumn(0), new InputColumn(1)), new boolean[]{ false, false }, new Boolean[]{ null, null });
        Projector projector = visitor.create(projection, txnCtx, ProjectionToProjectorVisitorTest.RAM_ACCOUNTING_CONTEXT, UUID.randomUUID());
        assertThat(projector, Matchers.instanceOf(SortingTopNProjector.class));
    }

    @Test
    public void testTopNProjectionToSortingProjector() throws Exception {
        List<Symbol> outputs = Arrays.asList(Literal.of("foo"), new InputColumn(0), new InputColumn(1));
        OrderedTopNProjection projection = new OrderedTopNProjection(NO_LIMIT, NO_OFFSET, outputs, Arrays.asList(new InputColumn(0), new InputColumn(1)), new boolean[]{ false, false }, new Boolean[]{ null, null });
        Projector projector = visitor.create(projection, txnCtx, ProjectionToProjectorVisitorTest.RAM_ACCOUNTING_CONTEXT, UUID.randomUUID());
        assertThat(projector, Matchers.instanceOf(SortingProjector.class));
    }

    @Test
    public void testAggregationProjector() throws Exception {
        AggregationProjection projection = new AggregationProjection(Arrays.asList(new Aggregation(avgInfo, avgInfo.returnType(), Collections.singletonList(new InputColumn(1))), new Aggregation(countInfo, countInfo.returnType(), Collections.singletonList(new InputColumn(0)))), RowGranularity.SHARD, AggregateMode.ITER_FINAL);
        Projector projector = visitor.create(projection, txnCtx, ProjectionToProjectorVisitorTest.RAM_ACCOUNTING_CONTEXT, UUID.randomUUID());
        assertThat(projector, Matchers.instanceOf(AggregationPipe.class));
        BatchIterator<Row> batchIterator = projector.apply(InMemoryBatchIterator.of(new CollectionBucket(Arrays.asList(RandomizedTest.$("foo", 10), RandomizedTest.$("bar", 20))), SENTINEL));
        TestingRowConsumer consumer = new TestingRowConsumer();
        consumer.accept(batchIterator, null);
        Bucket rows = consumer.getBucket();
        assertThat(rows.size(), Is.is(1));
        assertThat(rows, Matchers.contains(TestingHelpers.isRow(15.0, 2L)));
    }

    @Test
    public void testGroupProjector() throws Exception {
        // in(0)  in(1)      in(0),      in(2)
        // select  race, avg(age), count(race), gender  ... group by race, gender
        List<Symbol> keys = Arrays.asList(new InputColumn(0, DataTypes.STRING), new InputColumn(2, DataTypes.STRING));
        List<Aggregation> aggregations = Arrays.asList(new Aggregation(avgInfo, avgInfo.returnType(), Collections.singletonList(new InputColumn(1))), new Aggregation(countInfo, countInfo.returnType(), Collections.singletonList(new InputColumn(0))));
        GroupProjection projection = new GroupProjection(keys, aggregations, AggregateMode.ITER_FINAL, RowGranularity.CLUSTER);
        Projector projector = visitor.create(projection, txnCtx, ProjectionToProjectorVisitorTest.RAM_ACCOUNTING_CONTEXT, UUID.randomUUID());
        assertThat(projector, Matchers.instanceOf(GroupingProjector.class));
        // use a topN projection in order to get sorted outputs
        List<Symbol> outputs = Arrays.asList(new InputColumn(0, DataTypes.STRING), new InputColumn(1, DataTypes.STRING), new InputColumn(2, DataTypes.DOUBLE), new InputColumn(3, DataTypes.LONG));
        OrderedTopNProjection topNProjection = new OrderedTopNProjection(10, 0, outputs, ImmutableList.of(new InputColumn(2, DataTypes.DOUBLE)), new boolean[]{ false }, new Boolean[]{ null });
        Projector topNProjector = visitor.create(topNProjection, txnCtx, ProjectionToProjectorVisitorTest.RAM_ACCOUNTING_CONTEXT, UUID.randomUUID());
        String human = "human";
        String vogon = "vogon";
        String male = "male";
        String female = "female";
        List<Object[]> rows = new ArrayList<>();
        rows.add(RandomizedTest.$(human, 34, male));
        rows.add(RandomizedTest.$(human, 22, female));
        rows.add(RandomizedTest.$(vogon, 40, male));
        rows.add(RandomizedTest.$(vogon, 48, male));
        rows.add(RandomizedTest.$(human, 34, male));
        BatchIterator<Row> batchIterator = topNProjector.apply(projector.apply(InMemoryBatchIterator.of(new CollectionBucket(rows), SENTINEL)));
        TestingRowConsumer consumer = new TestingRowConsumer();
        consumer.accept(batchIterator, null);
        Bucket bucket = consumer.getBucket();
        assertThat(bucket, Matchers.contains(TestingHelpers.isRow(human, female, 22.0, 1L), TestingHelpers.isRow(human, male, 34.0, 2L), TestingHelpers.isRow(vogon, male, 44.0, 2L)));
    }

    @Test
    public void testFilterProjection() throws Exception {
        List<Symbol> arguments = Arrays.asList(Literal.of(2), new InputColumn(1));
        EqOperator op = ((EqOperator) (functions.get(null, NAME, arguments, SearchPath.pathWithPGCatalogAndDoc())));
        Function function = new Function(op.info(), arguments);
        FilterProjection projection = new FilterProjection(function, Arrays.asList(new InputColumn(0), new InputColumn(1)));
        Projector projector = visitor.create(projection, txnCtx, ProjectionToProjectorVisitorTest.RAM_ACCOUNTING_CONTEXT, UUID.randomUUID());
        assertThat(projector, Matchers.instanceOf(FilterProjector.class));
        List<Object[]> rows = new ArrayList<>();
        rows.add(RandomizedTest.$("human", 2));
        rows.add(RandomizedTest.$("vogon", 1));
        BatchIterator<Row> filteredBI = projector.apply(InMemoryBatchIterator.of(new CollectionBucket(rows), SENTINEL));
        TestingRowConsumer consumer = new TestingRowConsumer();
        consumer.accept(filteredBI, null);
        Bucket bucket = consumer.getBucket();
        assertThat(bucket.size(), Is.is(1));
    }
}
