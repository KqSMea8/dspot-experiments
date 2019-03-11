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
package io.crate.planner;


import AggregateMode.ITER_PARTIAL;
import CountAggregation.LongStateType.INSTANCE;
import DataTypes.LONG;
import ExecutionPhase.DIRECT_RESPONSE;
import ExecutionPhase.Type.HASH_JOIN;
import JoinType.INNER;
import Literal.BOOLEAN_TRUE;
import RowGranularity.DOC;
import RowGranularity.SHARD;
import SymbolType.INPUT_COLUMN;
import com.carrotsearch.hppc.IntIndexedContainer;
import com.google.common.collect.Iterables;
import io.crate.exceptions.UnsupportedFeatureException;
import io.crate.execution.dsl.phases.MergePhase;
import io.crate.execution.dsl.phases.NodeOperation;
import io.crate.execution.dsl.phases.NodeOperationTree;
import io.crate.execution.dsl.phases.RoutedCollectPhase;
import io.crate.execution.dsl.projection.AggregationProjection;
import io.crate.execution.dsl.projection.EvalProjection;
import io.crate.execution.dsl.projection.FetchProjection;
import io.crate.execution.dsl.projection.FilterProjection;
import io.crate.execution.dsl.projection.GroupProjection;
import io.crate.execution.dsl.projection.MergeCountProjection;
import io.crate.execution.dsl.projection.ProjectSetProjection;
import io.crate.execution.dsl.projection.Projection;
import io.crate.execution.dsl.projection.TopNProjection;
import io.crate.execution.engine.NodeOperationTreeGenerator;
import io.crate.expression.symbol.Aggregation;
import io.crate.expression.symbol.Function;
import io.crate.expression.symbol.InputColumn;
import io.crate.expression.symbol.Symbol;
import io.crate.metadata.Reference;
import io.crate.metadata.Routing;
import io.crate.planner.node.dql.Collect;
import io.crate.planner.node.dql.CountPlan;
import io.crate.planner.node.dql.QueryThenFetch;
import io.crate.planner.node.dql.join.Join;
import io.crate.planner.operators.LogicalPlan;
import io.crate.planner.operators.LogicalPlannerTest;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import io.crate.testing.SymbolMatchers;
import io.crate.testing.TestingHelpers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.Test;


public class SelectPlannerTest extends CrateDummyClusterServiceUnitTest {
    private SQLExecutor e;

    @Test
    public void testHandlerSideRouting() throws Exception {
        // just testing the dispatching here.. making sure it is not a ESSearchNode
        e.plan("select * from sys.cluster");
    }

    @Test
    public void testWherePKAndMatchDoesNotResultInESGet() throws Exception {
        ExecutionPlan plan = e.plan("select * from users where id in (1, 2, 3) and match(text, 'Hello')");
        assertThat(plan, Matchers.instanceOf(QueryThenFetch.class));
    }

    @Test
    public void testGetPlan() throws Exception {
        LogicalPlan plan = e.logicalPlan("select name from users where id = 1");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[name]\n" + "Get[doc.users | name | DocKeys{1}")));
    }

    @Test
    public void testGetWithVersion() throws Exception {
        LogicalPlan plan = e.logicalPlan("select name from users where id = 1 and _version = 1");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[name]\n" + "Get[doc.users | name | DocKeys{1, 1}")));
    }

    @Test
    public void testGetPlanStringLiteral() throws Exception {
        LogicalPlan plan = e.logicalPlan("select name from bystring where name = 'one'");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[name]\n" + "Get[doc.bystring | name | DocKeys{one}")));
    }

    @Test
    public void testGetPlanPartitioned() throws Exception {
        LogicalPlan plan = e.logicalPlan("select name, date from parted_pks where id = 1 and date = 0");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[name, date]\n" + "Get[doc.parted_pks | name, date | DocKeys{1, 0}")));
    }

    @Test
    public void testMultiGetPlan() throws Exception {
        LogicalPlan plan = e.logicalPlan("select name from users where id in (1, 2)");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[name]\n" + "Get[doc.users | name | DocKeys{1; 2}")));
    }

    @Test
    public void testGlobalAggregationPlan() throws Exception {
        Merge globalAggregate = e.plan("select count(name) from users");
        Collect collect = ((Collect) (globalAggregate.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collect.collectPhase()));
        assertEquals(INSTANCE, collectPhase.outputTypes().get(0));
        assertThat(collectPhase.maxRowGranularity(), Matchers.is(DOC));
        assertThat(collectPhase.projections().size(), Matchers.is(1));
        assertThat(collectPhase.projections().get(0), Matchers.instanceOf(AggregationProjection.class));
        assertThat(collectPhase.projections().get(0).requiredGranularity(), Matchers.is(SHARD));
        MergePhase mergePhase = globalAggregate.mergePhase();
        assertEquals(INSTANCE, Iterables.get(mergePhase.inputTypes(), 0));
        assertEquals(LONG, mergePhase.outputTypes().get(0));
    }

    @Test
    public void testShardSelectWithOrderBy() throws Exception {
        Merge merge = e.plan("select id from sys.shards order by id limit 10");
        Collect collect = ((Collect) (merge.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collect.collectPhase()));
        assertEquals(DataTypes.INTEGER, collectPhase.outputTypes().get(0));
        assertThat(collectPhase.maxRowGranularity(), Matchers.is(SHARD));
        assertThat(collectPhase.orderBy(), Matchers.notNullValue());
        List<Projection> projections = collectPhase.projections();
        assertThat(projections, Matchers.contains(Matchers.instanceOf(TopNProjection.class)));
    }

    @Test
    public void testCollectAndMergePlan() throws Exception {
        QueryThenFetch qtf = e.plan("select name from users where name = 'x' order by id limit 10");
        Merge merge = ((Merge) (qtf.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collectPhase()));
        assertThat(collectPhase.where().representation(), Matchers.is("Ref{doc.users.name, string} = x"));
        TopNProjection topNProjection = ((TopNProjection) (collectPhase.projections().get(0)));
        assertThat(topNProjection.limit(), Matchers.is(10));
        MergePhase mergePhase = merge.mergePhase();
        assertThat(mergePhase.outputTypes().size(), Matchers.is(1));
        assertEquals(DataTypes.STRING, mergePhase.outputTypes().get(0));
        assertTrue(mergePhase.finalProjection().isPresent());
        Projection lastProjection = mergePhase.finalProjection().get();
        assertThat(lastProjection, Matchers.instanceOf(FetchProjection.class));
        FetchProjection fetchProjection = ((FetchProjection) (lastProjection));
        assertThat(fetchProjection.outputs(), TestingHelpers.isSQL("FETCH(INPUT(0), doc.users._doc['name'])"));
    }

    @Test
    public void testCollectAndMergePlanNoFetch() throws Exception {
        // testing that a fetch projection is not added if all output symbols are included
        // at the orderBy symbols
        Merge merge = e.plan("select name from users where name = 'x' order by name limit 10");
        Collect collect = ((Collect) (merge.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collect.collectPhase()));
        assertThat(collectPhase.where().representation(), Matchers.is("Ref{doc.users.name, string} = x"));
        MergePhase mergePhase = merge.mergePhase();
        assertThat(mergePhase.outputTypes().size(), Matchers.is(1));
        assertEquals(DataTypes.STRING, mergePhase.outputTypes().get(0));
        assertTrue(mergePhase.finalProjection().isPresent());
        Projection lastProjection = mergePhase.finalProjection().get();
        assertThat(lastProjection, Matchers.instanceOf(TopNProjection.class));
        TopNProjection topNProjection = ((TopNProjection) (lastProjection));
        assertThat(topNProjection.outputs().size(), Matchers.is(1));
    }

    @Test
    public void testCollectAndMergePlanHighLimit() throws Exception {
        QueryThenFetch qtf = e.plan("select name from users limit 100000");
        Merge merge = ((Merge) (qtf.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collectPhase()));
        assertThat(collectPhase.nodePageSizeHint(), Matchers.is(100000));
        MergePhase mergePhase = merge.mergePhase();
        assertThat(mergePhase.projections().size(), Matchers.is(2));
        assertThat(mergePhase.finalProjection().get(), Matchers.instanceOf(FetchProjection.class));
        TopNProjection topN = ((TopNProjection) (mergePhase.projections().get(0)));
        assertThat(topN.limit(), Matchers.is(100000));
        assertThat(topN.offset(), Matchers.is(0));
        FetchProjection fetchProjection = ((FetchProjection) (mergePhase.projections().get(1)));
        // with offset
        qtf = e.plan("select name from users limit 100000 offset 20");
        merge = ((Merge) (qtf.subPlan()));
        collectPhase = ((RoutedCollectPhase) (collectPhase()));
        assertThat(collectPhase.nodePageSizeHint(), Matchers.is((100000 + 20)));
        mergePhase = merge.mergePhase();
        assertThat(mergePhase.projections().size(), Matchers.is(2));
        assertThat(mergePhase.finalProjection().get(), Matchers.instanceOf(FetchProjection.class));
        topN = ((TopNProjection) (mergePhase.projections().get(0)));
        assertThat(topN.limit(), Matchers.is(100000));
        assertThat(topN.offset(), Matchers.is(20));
        fetchProjection = ((FetchProjection) (mergePhase.projections().get(1)));
    }

    @Test
    public void testCollectAndMergePlanPartitioned() throws Exception {
        QueryThenFetch qtf = e.plan("select id, name, date from parted_pks where date > 0 and name = 'x' order by id limit 10");
        Merge merge = ((Merge) (qtf.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collectPhase()));
        List<String> indices = new ArrayList<>();
        Map<String, Map<String, IntIndexedContainer>> locations = collectPhase.routing().locations();
        for (Map.Entry<String, Map<String, IntIndexedContainer>> entry : locations.entrySet()) {
            indices.addAll(entry.getValue().keySet());
        }
        assertThat(indices, Matchers.contains(asIndexName()));
        assertThat(collectPhase.where().representation(), Matchers.is("Ref{doc.parted_pks.name, string} = x"));
        MergePhase mergePhase = merge.mergePhase();
        assertThat(mergePhase.outputTypes().size(), Matchers.is(3));
    }

    @Test
    public void testCollectAndMergePlanFunction() throws Exception {
        QueryThenFetch qtf = e.plan("select format('Hi, my name is %s', name), name from users where name = 'x' order by id limit 10");
        Merge merge = ((Merge) (qtf.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collectPhase()));
        assertThat(collectPhase.where().representation(), Matchers.is("Ref{doc.users.name, string} = x"));
        MergePhase mergePhase = merge.mergePhase();
        assertThat(mergePhase.outputTypes().size(), Matchers.is(2));
        assertEquals(DataTypes.STRING, mergePhase.outputTypes().get(0));
        assertEquals(DataTypes.STRING, mergePhase.outputTypes().get(1));
        assertTrue(mergePhase.finalProjection().isPresent());
        Projection lastProjection = mergePhase.finalProjection().get();
        assertThat(lastProjection, Matchers.instanceOf(FetchProjection.class));
        FetchProjection fetchProjection = ((FetchProjection) (lastProjection));
        assertThat(fetchProjection.outputs().size(), Matchers.is(2));
        assertThat(fetchProjection.outputs().get(0), SymbolMatchers.isFunction("format"));
        assertThat(fetchProjection.outputs().get(1), SymbolMatchers.isFetchRef(0, "_doc['name']"));
    }

    @Test
    public void testCountDistinctPlan() throws Exception {
        Merge globalAggregate = e.plan("select count(distinct name) from users");
        Collect collect = ((Collect) (globalAggregate.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collect.collectPhase()));
        Projection projection = collectPhase.projections().get(0);
        assertThat(projection, Matchers.instanceOf(AggregationProjection.class));
        AggregationProjection aggregationProjection = ((AggregationProjection) (projection));
        assertThat(aggregationProjection.aggregations().size(), Matchers.is(1));
        assertThat(aggregationProjection.mode(), Matchers.is(ITER_PARTIAL));
        Aggregation aggregation = aggregationProjection.aggregations().get(0);
        Symbol aggregationInput = aggregation.inputs().get(0);
        assertThat(aggregationInput.symbolType(), Matchers.is(INPUT_COLUMN));
        assertThat(collectPhase.toCollect().get(0), Matchers.instanceOf(Reference.class));
        assertThat(column().name(), Matchers.is("name"));
        MergePhase mergePhase = globalAggregate.mergePhase();
        assertThat(mergePhase.projections().size(), Matchers.is(2));
        Projection projection1 = mergePhase.projections().get(1);
        assertThat(projection1, Matchers.instanceOf(EvalProjection.class));
        Symbol collection_count = projection1.outputs().get(0);
        assertThat(collection_count, Matchers.instanceOf(Function.class));
    }

    @Test
    public void testGlobalAggregationHaving() throws Exception {
        Merge globalAggregate = e.plan("select avg(date) from users having min(date) > '1970-01-01'");
        Collect collect = ((Collect) (globalAggregate.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collect.collectPhase()));
        assertThat(collectPhase.projections().size(), Matchers.is(1));
        assertThat(collectPhase.projections().get(0), Matchers.instanceOf(AggregationProjection.class));
        MergePhase localMergeNode = globalAggregate.mergePhase();
        assertThat(localMergeNode.projections(), Matchers.contains(Matchers.instanceOf(AggregationProjection.class), Matchers.instanceOf(FilterProjection.class), Matchers.instanceOf(EvalProjection.class)));
        AggregationProjection aggregationProjection = ((AggregationProjection) (localMergeNode.projections().get(0)));
        assertThat(aggregationProjection.aggregations().size(), Matchers.is(2));
        FilterProjection filterProjection = ((FilterProjection) (localMergeNode.projections().get(1)));
        assertThat(filterProjection.outputs().size(), Matchers.is(2));
        assertThat(filterProjection.outputs().get(0), Matchers.instanceOf(InputColumn.class));
        InputColumn inputColumn = ((InputColumn) (filterProjection.outputs().get(0)));
        assertThat(inputColumn.index(), Matchers.is(0));
        EvalProjection evalProjection = ((EvalProjection) (localMergeNode.projections().get(2)));
        assertThat(evalProjection.outputs().size(), Matchers.is(1));
    }

    @Test
    public void testCountOnPartitionedTable() throws Exception {
        CountPlan plan = e.plan("select count(*) from parted where date = 1395874800000");
        assertThat(plan.countPhase().routing().locations().entrySet().stream().flatMap(( e) -> e.getValue().keySet().stream()).collect(Collectors.toSet()), Matchers.contains(Matchers.is(".partitioned.parted.04732cpp6ks3ed1o60o30c1g")));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSelectPartitionedTableOrderByPartitionedColumnInFunction() throws Exception {
        e.plan("select name from parted order by year(date)");
    }

    @Test(expected = UnsupportedFeatureException.class)
    public void testQueryRequiresScalar() throws Exception {
        // only scalar functions are allowed on system tables because we have no lucene queries
        e.plan("select * from sys.shards where match(table_name, 'characters')");
    }

    @Test
    public void testSortOnUnknownColumn() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("Cannot ORDER BY 'details['unknown_column']': invalid data type 'undefined'.");
        e.plan("select details from ignored_nested order by details['unknown_column']");
    }

    @Test
    public void testSelectAnalyzedReferenceInFunctionAggregation() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot select analyzed column 'text' within grouping or aggregations");
        e.plan("select min(substr(text, 0, 2)) from users");
    }

    @Test
    public void testGlobalAggregateWithWhereOnPartitionColumn() throws Exception {
        Merge merge = e.plan("select min(name) from parted where date >= 1395961200000");
        Collect collect = ((Collect) (merge.subPlan()));
        Routing routing = routing();
        assertThat(routing.locations().values().stream().flatMap(( shardsByIndex) -> shardsByIndex.keySet().stream()).collect(Collectors.toSet()), Matchers.contains(Matchers.is(".partitioned.parted.04732cpp6ksjcc9i60o30c1g")));
    }

    @Test
    public void testHasNoResultFromHaving() throws Exception {
        Merge merge = e.plan("select min(name) from users having 1 = 2");
        assertThat(merge.mergePhase().projections().get(1), Matchers.instanceOf(FilterProjection.class));
        assertThat(query(), TestingHelpers.isSQL("false"));
    }

    @Test
    public void testShardQueueSizeCalculation() throws Exception {
        Merge merge = e.plan("select name from users order by name limit 500");
        Collect collect = ((Collect) (merge.subPlan()));
        int shardQueueSize = ((RoutedCollectPhase) (collect.collectPhase())).shardQueueSize(collect.collectPhase().nodeIds().iterator().next());
        assertThat(shardQueueSize, Matchers.is(375));
    }

    @Test
    public void testQAFPagingIsEnabledOnHighLimit() throws Exception {
        Merge plan = e.plan("select name from users order by name limit 1000000");
        assertThat(plan.mergePhase().nodeIds().size(), Matchers.is(1));// mergePhase with executionNode = paging enabled

        Collect collect = ((Collect) (plan.subPlan()));
        assertThat(nodePageSizeHint(), Matchers.is(750000));
    }

    @Test
    public void testQAFPagingIsEnabledOnHighOffset() throws Exception {
        Merge merge = e.plan("select name from users order by name limit 10 offset 1000000");
        Collect collect = ((Collect) (merge.subPlan()));
        assertThat(merge.mergePhase().nodeIds().size(), Matchers.is(1));// mergePhase with executionNode = paging enabled

        assertThat(nodePageSizeHint(), Matchers.is(750007));
    }

    @Test
    public void testQTFPagingIsEnabledOnHighLimit() throws Exception {
        QueryThenFetch qtf = e.plan("select name, date from users order by name limit 1000000");
        Merge merge = ((Merge) (qtf.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collectPhase()));
        assertThat(merge.mergePhase().nodeIds().size(), Matchers.is(1));// mergePhase with executionNode = paging enabled

        assertThat(collectPhase.nodePageSizeHint(), Matchers.is(750000));
    }

    @Test
    public void testSelectFromUnnestResultsInTableFunctionPlan() throws Exception {
        Collect collect = e.plan("select * from unnest([1, 2], ['Arthur', 'Trillian'])");
        assertNotNull(collect);
        assertThat(collect.collectPhase().toCollect(), Matchers.contains(SymbolMatchers.isReference("col1"), SymbolMatchers.isReference("col2")));
    }

    @Test
    public void testSoftLimitIsApplied() throws Exception {
        QueryThenFetch qtf = e.plan("select * from users", UUID.randomUUID(), 10, 0);
        Merge merge = ((Merge) (qtf.subPlan()));
        assertThat(merge.mergePhase().projections(), Matchers.contains(Matchers.instanceOf(TopNProjection.class), Matchers.instanceOf(FetchProjection.class)));
        TopNProjection topNProjection = ((TopNProjection) (merge.mergePhase().projections().get(0)));
        assertThat(topNProjection.limit(), Matchers.is(10));
        qtf = e.plan("select * from users limit 5", UUID.randomUUID(), 10, 0);
        merge = ((Merge) (qtf.subPlan()));
        assertThat(merge.mergePhase().projections(), Matchers.contains(Matchers.instanceOf(TopNProjection.class), Matchers.instanceOf(FetchProjection.class)));
        topNProjection = ((TopNProjection) (merge.mergePhase().projections().get(0)));
        assertThat(topNProjection.limit(), Matchers.is(5));
    }

    @Test
    public void testReferenceToNestedAggregatedField() throws Exception {
        Collect collect = e.plan(("select ii, xx from ( " + (("  select i + i as ii, xx from (" + "    select i, sum(x) as xx from t1 group by i) as t) as tt ") + "where (ii * 2) > 4 and (xx * 2) > 120")));
        assertThat("would require merge with more than 1 nodeIds", collect.nodeIds().size(), Matchers.is(1));
        List<Projection> projections = collect.collectPhase().projections();
        assertThat(projections, // parallel on shard-level
        // node-level
        Matchers.contains(Matchers.instanceOf(GroupProjection.class), Matchers.instanceOf(GroupProjection.class), Matchers.instanceOf(EvalProjection.class), Matchers.instanceOf(FilterProjection.class)));
    }

    @Test
    public void test3TableJoinQuerySplitting() throws Exception {
        QueryThenFetch qtf = e.plan(("select" + (((((((((("  u1.id as u1, " + "  u2.id as u2, ") + "  u3.id as u3 ") + "from ") + "  users u1,") + "  users u2,") + "  users u3 ") + "where ") + "  u1.name = 'Arthur'") + "  and u2.id = u1.id") + "  and u2.name = u1.name")));
        Join outerNl = ((Join) (qtf.subPlan()));
        Join innerNl = ((Join) (outerNl.left()));
        assertThat(innerNl.joinPhase().joinCondition(), TestingHelpers.isSQL("((INPUT(0) = INPUT(2)) AND (INPUT(1) = INPUT(3)))"));
        assertThat(innerNl.joinPhase().projections().size(), Matchers.is(1));
        assertThat(innerNl.joinPhase().projections().get(0), Matchers.instanceOf(EvalProjection.class));
        assertThat(outerNl.joinPhase().joinCondition(), Matchers.nullValue());
        assertThat(outerNl.joinPhase().projections().size(), Matchers.is(2));
        assertThat(outerNl.joinPhase().projections().get(0), Matchers.instanceOf(EvalProjection.class));
        assertThat(outerNl.joinPhase().projections().get(1), Matchers.instanceOf(FetchProjection.class));
    }

    @Test
    public void testOuterJoinToInnerJoinRewrite() throws Exception {
        // disable hash joins otherwise it will be a distributed join and the plan differs
        e.getSessionContext().setHashJoinEnabled(false);
        QueryThenFetch qtf = e.plan(("select u1.text, concat(u2.text, '_foo') " + (("from users u1 left join users u2 on u1.id = u2.id " + "where u2.name = 'Arthur'") + "and u2.id > 1 ")));
        Join nl = ((Join) (subPlan()));
        assertThat(nl.joinPhase().joinType(), Matchers.is(INNER));
        Collect rightCM = ((Collect) (nl.right()));
        assertThat(where(), TestingHelpers.isSQL("((doc.users.name = 'Arthur') AND (doc.users.id > 1))"));
        // doesn't contain "name" because whereClause is pushed down,
        // but still contains "id" because it is in the joinCondition
        assertThat(rightCM.collectPhase().toCollect(), Matchers.contains(SymbolMatchers.isReference("_fetchid"), SymbolMatchers.isReference("id")));
        Collect left = ((Collect) (nl.left()));
        assertThat(left.collectPhase().toCollect(), Matchers.contains(SymbolMatchers.isReference("_fetchid"), SymbolMatchers.isReference("id")));
    }

    @Test
    public void testShardSelect() throws Exception {
        Merge merge = e.plan("select id from sys.shards");
        Collect collect = ((Collect) (merge.subPlan()));
        RoutedCollectPhase collectPhase = ((RoutedCollectPhase) (collect.collectPhase()));
        assertThat(collectPhase.maxRowGranularity(), Matchers.is(SHARD));
    }

    @Test
    public void testGlobalCountPlan() throws Exception {
        CountPlan plan = e.plan("select count(*) from users");
        assertThat(where(), Matchers.equalTo(BOOLEAN_TRUE));
        assertThat(plan.mergePhase().projections().size(), Matchers.is(1));
        assertThat(plan.mergePhase().projections().get(0), Matchers.instanceOf(MergeCountProjection.class));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testLimitThatIsBiggerThanPageSizeCausesQTFPUshPlan() throws Exception {
        QueryThenFetch qtf = e.plan("select * from users limit 2147483647 ");
        Merge merge = ((Merge) (qtf.subPlan()));
        assertThat(merge.mergePhase().nodeIds().size(), Matchers.is(1));
        String localNodeId = merge.mergePhase().nodeIds().iterator().next();
        NodeOperationTree operationTree = NodeOperationTreeGenerator.fromPlan(qtf, localNodeId);
        NodeOperation nodeOperation = operationTree.nodeOperations().iterator().next();
        // paging -> must not use direct response
        assertThat(nodeOperation.downstreamNodes(), Matchers.not(Matchers.contains(DIRECT_RESPONSE)));
        qtf = e.plan("select * from users limit 2");
        localNodeId = qtf.subPlan().resultDescription().nodeIds().iterator().next();
        operationTree = NodeOperationTreeGenerator.fromPlan(qtf, localNodeId);
        nodeOperation = operationTree.nodeOperations().iterator().next();
        // no paging -> can use direct response
        assertThat(nodeOperation.downstreamNodes(), Matchers.contains(DIRECT_RESPONSE));
    }

    @Test
    public void testAggregationOnGeneratedColumns() throws Exception {
        Merge merge = e.plan("select sum(profit) from gc_table");
        Collect collect = ((Collect) (merge.subPlan()));
        List<Projection> projections = collect.collectPhase().projections();
        assertThat(projections, // iter-partial on shard level
        Matchers.contains(Matchers.instanceOf(AggregationProjection.class)));
        assertThat(merge.mergePhase().projections(), Matchers.contains(Matchers.instanceOf(AggregationProjection.class)));
        assertThat(aggregations().get(0).inputs().get(0), TestingHelpers.isSQL("INPUT(0)"));
    }

    @Test
    public void testGlobalAggregationOn3TableJoinWithImplicitJoinConditions() {
        Merge plan = e.plan(("select count(*) from users t1, users t2, users t3 " + "where t1.id = t2.id and t2.id = t3.id"));
        assertThat(plan.subPlan(), Matchers.instanceOf(Join.class));
        Join outerNL = ((Join) (plan.subPlan()));
        assertThat(outerNL.joinPhase().joinCondition(), TestingHelpers.isSQL("(INPUT(1) = INPUT(2))"));
        assertThat(outerNL.joinPhase().projections().size(), Matchers.is(2));
        assertThat(outerNL.joinPhase().projections().get(0), Matchers.instanceOf(EvalProjection.class));
        assertThat(outerNL.joinPhase().projections().get(1), Matchers.instanceOf(AggregationProjection.class));
        assertThat(outerNL.joinPhase().outputTypes().size(), Matchers.is(1));
        assertThat(outerNL.joinPhase().outputTypes().get(0), Matchers.is(INSTANCE));
        Join innerNL = ((Join) (outerNL.left()));
        assertThat(innerNL.joinPhase().joinCondition(), TestingHelpers.isSQL("(INPUT(0) = INPUT(1))"));
        assertThat(innerNL.joinPhase().projections().size(), Matchers.is(1));
        assertThat(innerNL.joinPhase().projections().get(0), Matchers.instanceOf(EvalProjection.class));
        assertThat(innerNL.joinPhase().outputTypes().size(), Matchers.is(2));
        assertThat(innerNL.joinPhase().outputTypes().get(0), Matchers.is(LONG));
        plan = e.plan(("select count(t1.other_id) from users t1, users t2, users t3 " + "where t1.id = t2.id and t2.id = t3.id"));
        assertThat(plan.subPlan(), Matchers.instanceOf(Join.class));
        outerNL = ((Join) (plan.subPlan()));
        assertThat(outerNL.joinPhase().joinCondition(), TestingHelpers.isSQL("(INPUT(2) = INPUT(3))"));
        assertThat(outerNL.joinPhase().projections().size(), Matchers.is(2));
        assertThat(outerNL.joinPhase().projections().get(0), Matchers.instanceOf(EvalProjection.class));
        assertThat(outerNL.joinPhase().projections().get(1), Matchers.instanceOf(AggregationProjection.class));
        assertThat(outerNL.joinPhase().outputTypes().size(), Matchers.is(1));
        assertThat(outerNL.joinPhase().outputTypes().get(0), Matchers.is(INSTANCE));
        innerNL = ((Join) (outerNL.left()));
        assertThat(innerNL.joinPhase().joinCondition(), TestingHelpers.isSQL("(INPUT(0) = INPUT(2))"));
        assertThat(innerNL.joinPhase().projections().size(), Matchers.is(1));
        assertThat(innerNL.joinPhase().projections().get(0), Matchers.instanceOf(EvalProjection.class));
        assertThat(innerNL.joinPhase().outputTypes().size(), Matchers.is(3));
        assertThat(innerNL.joinPhase().outputTypes().get(0), Matchers.is(LONG));
        assertThat(innerNL.joinPhase().outputTypes().get(1), Matchers.is(LONG));
    }

    @Test
    public void test2TableJoinWithNoMatch() throws Exception {
        QueryThenFetch qtf = e.plan("select * from users t1, users t2 WHERE 1=2");
        Join nl = ((Join) (qtf.subPlan()));
        assertThat(nl.left(), Matchers.instanceOf(Collect.class));
        assertThat(nl.right(), Matchers.instanceOf(Collect.class));
        assertThat(where(), TestingHelpers.isSQL("false"));
        assertThat(where(), TestingHelpers.isSQL("false"));
    }

    @Test
    public void test3TableJoinWithNoMatch() throws Exception {
        QueryThenFetch qtf = e.plan("select * from users t1, users t2, users t3 WHERE 1=2");
        Join outer = ((Join) (qtf.subPlan()));
        assertThat(where(), TestingHelpers.isSQL("false"));
        Join inner = ((Join) (outer.left()));
        assertThat(where(), SymbolMatchers.isLiteral(false));
        assertThat(where(), SymbolMatchers.isLiteral(false));
    }

    @Test
    public void testGlobalAggregateOn2TableJoinWithNoMatch() throws Exception {
        Join nl = e.plan("select count(*) from users t1, users t2 WHERE 1=2");
        assertThat(nl.left(), Matchers.instanceOf(Collect.class));
        assertThat(nl.right(), Matchers.instanceOf(Collect.class));
        assertThat(where(), SymbolMatchers.isLiteral(false));
        assertThat(where(), SymbolMatchers.isLiteral(false));
    }

    @Test
    public void testGlobalAggregateOn3TableJoinWithNoMatch() throws Exception {
        Join outer = e.plan("select count(*) from users t1, users t2, users t3 WHERE 1=2");
        Join inner = ((Join) (outer.left()));
        assertThat(where(), SymbolMatchers.isLiteral(false));
        assertThat(where(), SymbolMatchers.isLiteral(false));
        assertThat(where(), SymbolMatchers.isLiteral(false));
    }

    @Test
    public void testFilterOnPKSubsetResultsInPKLookupPlanIfTheOtherPKPartIsGenerated() {
        LogicalPlan plan = e.logicalPlan("select 1 from t_pk_part_generated where ts = 0");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[1]\n" + "Get[doc.t_pk_part_generated | 1 | DocKeys{0, 0}")));
    }

    @Test
    public void testInnerJoinResultsInHashJoinIfHashJoinIsEnabled() {
        e.getSessionContext().setHashJoinEnabled(true);
        Join join = e.plan("select t2.b, t1.a from t1 inner join t2 on t1.i = t2.i order by 1, 2");
        assertThat(join.joinPhase().type(), Matchers.is(HASH_JOIN));
    }

    @Test
    public void testUnnestInSelectListResultsInPlanWithProjectSetOperator() {
        LogicalPlan plan = e.logicalPlan("select unnest([1, 2])");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[unnest([1, 2])]\n" + ("ProjectSet[unnest([1, 2])]\n" + "Collect[.empty_row | [] | All]\n"))));
        Symbol output = plan.outputs().get(0);
        assertThat(output.valueType(), Matchers.is(LONG));
    }

    @Test
    public void testScalarCanBeUsedAroundTableGeneratingFunctionInSelectList() {
        LogicalPlan plan = e.logicalPlan("select unnest([1, 2]) + 1");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[(unnest([1, 2]) + 1)]\n" + (("FetchOrEval[(unnest([1, 2]) + 1)]\n" + "ProjectSet[unnest([1, 2])]\n") + "Collect[.empty_row | [] | All]\n"))));
    }

    @Test
    public void testAggregationOnTopOfTableFunctionIsNotPossibleWithoutSeparateSubQuery() {
        expectedException.expectMessage("Cannot use table functions inside aggregates");
        e.logicalPlan("select sum(unnest([1, 2]))");
    }

    @Test
    public void testTableFunctionIsExecutedAfterAggregation() {
        LogicalPlan plan = e.logicalPlan("select count(*), generate_series(1, 2) from users");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[count(*), generate_series(1, 2)]\n" + (("FetchOrEval[count(*), generate_series(1, 2)]\n" + "ProjectSet[generate_series(1, 2) | count(*)]\n") + "Count[doc.users | All]\n"))));
    }

    @Test
    public void testAggregationCanBeUsedAsArgumentToTableFunction() {
        LogicalPlan plan = e.logicalPlan("select count(name), generate_series(1, count(name)) from users");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[count(name), generate_series(1, count(name))]\n" + ((("FetchOrEval[count(name), generate_series(1, count(name))]\n" + "ProjectSet[generate_series(1, count(name)) | count(name)]\n") + "Aggregate[count(name)]\n") + "Collect[doc.users | [name] | All]\n"))));
    }

    @Test
    public void testOrderByOnTableFunctionMustOrderAfterProjectSet() {
        LogicalPlan plan = e.logicalPlan("select unnest([1, 2]) from sys.nodes order by 1");
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("RootBoundary[unnest([1, 2])]\n" + (("OrderBy[unnest([1, 2]) ASC]\n" + "ProjectSet[unnest([1, 2])]\n") + "Collect[sys.nodes | [] | All]\n"))));
    }

    @Test
    public void testSelectingTableFunctionAndStandaloneColumnOnUserTablesCanDealWithFetchId() {
        QueryThenFetch qtf = e.plan("select unnest([1, 2]), name from users");
        Merge merge = ((Merge) (qtf.subPlan()));
        Collect collect = ((Collect) (merge.subPlan()));
        assertThat(collect.collectPhase().projections(), Matchers.contains(Matchers.instanceOf(ProjectSetProjection.class)));
        assertThat(merge.mergePhase().projections(), Matchers.contains(Matchers.instanceOf(FetchProjection.class)));
    }

    @Test
    public void testUnnestCannotReturnMultipleColumnsIfUsedInSelectList() {
        expectedException.expectMessage("Table function used in select list must not return multiple columns");
        e.logicalPlan("select unnest([1, 2], [3, 4])");
    }
}
