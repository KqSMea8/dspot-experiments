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
package io.crate.planner.operators;


import Row.EMPTY;
import TopN.NO_LIMIT;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import io.crate.analyze.QueriedTable;
import io.crate.analyze.TableDefinitions;
import io.crate.expression.symbol.Literal;
import io.crate.planner.Merge;
import io.crate.planner.PlannerContext;
import io.crate.planner.TableStats;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.ProjectionMatchers;
import io.crate.testing.SQLExecutor;
import java.util.Collections;
import org.hamcrest.Matchers;
import org.junit.Test;


public class LimitTest extends CrateDummyClusterServiceUnitTest {
    @Test
    public void testLimitOnLimitOperator() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService, 2, RandomizedTest.getRandom()).addTable(TableDefinitions.USER_TABLE_DEFINITION).build();
        QueriedTable queriedDocTable = e.analyze("select name from users");
        LogicalPlan plan = Limit.create(Limit.create(Collect.create(queriedDocTable, queriedDocTable.outputs(), queriedDocTable.where()), Literal.of(10L), Literal.of(5L)), Literal.of(20L), Literal.of(7L)).build(new TableStats(), Collections.emptySet());
        assertThat(plan, LogicalPlannerTest.isPlan(e.functions(), ("Limit[20;7]\n" + ("Limit[10;5]\n" + "Collect[doc.users | [_fetchid] | All]\n"))));
        PlannerContext ctx = e.getPlannerContext(clusterService.state());
        Merge merge = ((Merge) (plan.build(ctx, new io.crate.execution.dsl.projection.builder.ProjectionBuilder(e.functions()), NO_LIMIT, 0, null, null, EMPTY, SubQueryResults.EMPTY)));
        io.crate.planner.node.dql.Collect collect = ((io.crate.planner.node.dql.Collect) (merge.subPlan()));
        assertThat(collect.collectPhase().projections(), Matchers.contains(ProjectionMatchers.isTopN(15, 0)));
        // noinspection unchecked
        assertThat(merge.mergePhase().projections(), Matchers.contains(ProjectionMatchers.isTopN(10, 5), ProjectionMatchers.isTopN(20, 7)));
    }
}
