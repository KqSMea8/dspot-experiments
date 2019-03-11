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
package io.crate.action.sql;


import DataTypes.INTEGER;
import DataTypes.LONG;
import DataTypes.UNDEFINED;
import ParamTypeHints.EMPTY;
import Session.DescribeResult;
import Session.ParameterTypeExtractor;
import io.crate.analyze.AnalyzedStatement;
import io.crate.analyze.Relations;
import io.crate.analyze.TableDefinitions;
import io.crate.execution.engine.collect.stats.JobsLogs;
import io.crate.expression.symbol.Literal;
import io.crate.expression.symbol.Symbol;
import io.crate.planner.DependencyCarrier;
import io.crate.sql.parser.SqlParser;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import io.crate.types.DataType;
import io.crate.types.DataTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.elasticsearch.threadpool.ThreadPool;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;


public class SessionTest extends CrateDummyClusterServiceUnitTest {
    @Test
    public void testParameterTypeExtractorNotApplicable() {
        Session.ParameterTypeExtractor typeExtractor = new Session.ParameterTypeExtractor();
        assertThat(typeExtractor.getParameterTypes(( s) -> {
        }).length, Matchers.is(0));
    }

    @Test
    public void testParameterTypeExtractor() {
        Session.ParameterTypeExtractor typeExtractor = new Session.ParameterTypeExtractor();
        List<Symbol> symbolsToVisit = new ArrayList<>();
        symbolsToVisit.add(Literal.of(1));
        symbolsToVisit.add(Literal.of("foo"));
        symbolsToVisit.add(new io.crate.expression.symbol.ParameterSymbol(1, DataTypes.LONG));
        symbolsToVisit.add(new io.crate.expression.symbol.ParameterSymbol(0, DataTypes.INTEGER));
        symbolsToVisit.add(new io.crate.expression.symbol.ParameterSymbol(3, DataTypes.STRING));
        symbolsToVisit.add(Literal.of("bar"));
        symbolsToVisit.add(new io.crate.expression.symbol.ParameterSymbol(2, DataTypes.DOUBLE));
        symbolsToVisit.add(new io.crate.expression.symbol.ParameterSymbol(1, DataTypes.LONG));
        symbolsToVisit.add(new io.crate.expression.symbol.ParameterSymbol(0, DataTypes.INTEGER));
        symbolsToVisit.add(Literal.of(1.2));
        Consumer<Consumer<? super Symbol>> symbolVisitor = ( c) -> {
            for (Symbol symbol : symbolsToVisit) {
                c.accept(symbol);
            }
        };
        DataType[] parameterTypes = typeExtractor.getParameterTypes(symbolVisitor);
        assertThat(parameterTypes, Matchers.equalTo(new DataType[]{ DataTypes.INTEGER, DataTypes.LONG, DataTypes.DOUBLE, DataTypes.STRING }));
        symbolsToVisit.add(new io.crate.expression.symbol.ParameterSymbol(4, DataTypes.BOOLEAN));
        parameterTypes = typeExtractor.getParameterTypes(symbolVisitor);
        assertThat(parameterTypes, Matchers.equalTo(new DataType[]{ DataTypes.INTEGER, DataTypes.LONG, DataTypes.DOUBLE, DataTypes.STRING, DataTypes.BOOLEAN }));
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("The assembled list of ParameterSymbols is invalid.");
        // remove the double parameter => make the input invalid
        symbolsToVisit.remove(6);
        typeExtractor.getParameterTypes(symbolVisitor);
    }

    @Test
    public void testGetParamType() {
        SQLExecutor sqlExecutor = SQLExecutor.builder(clusterService).build();
        DependencyCarrier executor = Mockito.mock(DependencyCarrier.class);
        Session session = new Session(sqlExecutor.analyzer, sqlExecutor.planner, new JobsLogs(() -> false), false, executor, SessionContext.systemSessionContext());
        session.parse("S_1", "Select 1 + ? + ?;", Collections.emptyList());
        assertThat(session.getParamType("S_1", 0), Matchers.is(UNDEFINED));
        assertThat(session.getParamType("S_1", 2), Matchers.is(UNDEFINED));
        Session.DescribeResult describe = session.describe('S', "S_1");
        assertThat(describe.getParameters(), Matchers.equalTo(new DataType[]{ DataTypes.LONG, DataTypes.LONG }));
        assertThat(session.getParamType("S_1", 0), Matchers.is(LONG));
        assertThat(session.getParamType("S_1", 1), Matchers.is(LONG));
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Requested parameter index exceeds the number of parameters");
        assertThat(session.getParamType("S_1", 3), Matchers.is(UNDEFINED));
    }

    @Test
    public void testExtractTypesFromDelete() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).addTable(TableDefinitions.USER_TABLE_DEFINITION).build();
        AnalyzedStatement analyzedStatement = e.analyzer.unboundAnalyze(SqlParser.createStatement("delete from users where name = ?"), SessionContext.systemSessionContext(), EMPTY);
        Session.ParameterTypeExtractor typeExtractor = new Session.ParameterTypeExtractor();
        DataType[] parameterTypes = typeExtractor.getParameterTypes(analyzedStatement::visitSymbols);
        assertThat(parameterTypes, Matchers.is(new DataType[]{ DataTypes.STRING }));
    }

    @Test
    public void testExtractTypesFromUpdate() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).addTable(TableDefinitions.USER_TABLE_DEFINITION).build();
        AnalyzedStatement analyzedStatement = e.analyzer.unboundAnalyze(SqlParser.createStatement("update users set name = ? || '_updated' where id = ?"), SessionContext.systemSessionContext(), EMPTY);
        Session.ParameterTypeExtractor typeExtractor = new Session.ParameterTypeExtractor();
        DataType[] parameterTypes = typeExtractor.getParameterTypes(analyzedStatement::visitSymbols);
        assertThat(parameterTypes, Matchers.is(new DataType[]{ DataTypes.STRING, DataTypes.LONG }));
    }

    @Test
    public void testExtractTypesFromInsertValues() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).addTable(TableDefinitions.USER_TABLE_DEFINITION).build();
        AnalyzedStatement analyzedStatement = e.analyzer.unboundAnalyze(SqlParser.createStatement("INSERT INTO users (id, name) values (?, ?)"), SessionContext.systemSessionContext(), EMPTY);
        Session.ParameterTypeExtractor typeExtractor = new Session.ParameterTypeExtractor();
        DataType[] parameterTypes = typeExtractor.getParameterTypes(analyzedStatement::visitSymbols);
        assertThat(parameterTypes, Matchers.is(new DataType[]{ DataTypes.LONG, DataTypes.STRING }));
    }

    @Test
    public void testExtractTypesFromInsertFromQuery() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).addTable(TableDefinitions.USER_TABLE_DEFINITION).addDocTable(TableDefinitions.USER_TABLE_INFO_CLUSTERED_BY_ONLY).build();
        AnalyzedStatement analyzedStatement = e.analyzer.unboundAnalyze(SqlParser.createStatement(("INSERT INTO users (id, name) (SELECT id, name FROM users_clustered_by_only " + "WHERE name = ?)")), SessionContext.systemSessionContext(), EMPTY);
        Session.ParameterTypeExtractor typeExtractor = new Session.ParameterTypeExtractor();
        DataType[] parameterTypes = typeExtractor.getParameterTypes(analyzedStatement::visitSymbols);
        assertThat(parameterTypes, Matchers.is(new DataType[]{ DataTypes.STRING }));
    }

    @Test
    public void testExtractTypesFromInsertWithOnDuplicateKey() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).addTable(TableDefinitions.USER_TABLE_DEFINITION).addDocTable(TableDefinitions.USER_TABLE_INFO_CLUSTERED_BY_ONLY).build();
        AnalyzedStatement analyzedStatement = e.analyzer.unboundAnalyze(SqlParser.createStatement(("INSERT INTO users (id, name) values (?, ?) " + "ON CONFLICT (id) DO UPDATE SET name = ?")), SessionContext.systemSessionContext(), EMPTY);
        Session.ParameterTypeExtractor typeExtractor = new Session.ParameterTypeExtractor();
        DataType[] parameterTypes = typeExtractor.getParameterTypes(analyzedStatement::visitSymbols);
        assertThat(parameterTypes, Matchers.is(new DataType[]{ DataTypes.LONG, DataTypes.STRING, DataTypes.STRING }));
        analyzedStatement = e.analyzer.unboundAnalyze(SqlParser.createStatement(("INSERT INTO users (id, name) (SELECT id, name FROM users_clustered_by_only " + "WHERE name = ?) ON CONFLICT (id) DO UPDATE SET name = ?")), SessionContext.systemSessionContext(), EMPTY);
        typeExtractor = new Session.ParameterTypeExtractor();
        parameterTypes = typeExtractor.getParameterTypes(analyzedStatement::visitSymbols);
        assertThat(parameterTypes, Matchers.is(new DataType[]{ DataTypes.STRING, DataTypes.STRING }));
    }

    @Test
    public void testTypesCanBeResolvedIfParametersAreInSubRelation() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).build();
        AnalyzedStatement stmt = e.analyzer.unboundAnalyze(SqlParser.createStatement("select * from (select $1::int + $2) t"), SessionContext.systemSessionContext(), EMPTY);
        DataType[] parameterTypes = new Session.ParameterTypeExtractor().getParameterTypes(( consumer) -> Relations.traverseDeepSymbols(stmt, consumer));
        assertThat(parameterTypes, Matchers.arrayContaining(Matchers.is(INTEGER), Matchers.is(INTEGER)));
    }

    @Test
    public void testTypesCanBeResolvedIfParametersAreInSubRelationOfInsertStatement() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).addTable("create table t (x int)").build();
        AnalyzedStatement stmt = e.analyzer.unboundAnalyze(SqlParser.createStatement("insert into t (x) (select * from (select $1::int + $2) t)"), SessionContext.systemSessionContext(), EMPTY);
        DataType[] parameterTypes = new Session.ParameterTypeExtractor().getParameterTypes(( consumer) -> Relations.traverseDeepSymbols(stmt, consumer));
        assertThat(parameterTypes, Matchers.arrayContaining(Matchers.is(INTEGER), Matchers.is(INTEGER)));
    }

    @Test
    public void testTypesCanBeResolvedIfParametersAreInSubQueryInDeleteStatement() throws Exception {
        SQLExecutor e = SQLExecutor.builder(clusterService).addTable("create table t (x int)").build();
        AnalyzedStatement stmt = e.analyzer.unboundAnalyze(SqlParser.createStatement("delete from t where x = (select $1::long)"), SessionContext.systemSessionContext(), EMPTY);
        DataType[] parameterTypes = new Session.ParameterTypeExtractor().getParameterTypes(( consumer) -> Relations.traverseDeepSymbols(stmt, consumer));
        assertThat(parameterTypes, Matchers.arrayContaining(Matchers.is(LONG)));
    }

    @Test
    public void testProperCleanupOnSessionClose() {
        SQLExecutor sqlExecutor = SQLExecutor.builder(clusterService).build();
        DependencyCarrier executor = Mockito.mock(DependencyCarrier.class);
        Session session = new Session(sqlExecutor.analyzer, sqlExecutor.planner, new JobsLogs(() -> false), false, executor, SessionContext.systemSessionContext());
        session.parse("S_1", "select name from sys.cluster;", Collections.emptyList());
        session.bind("Portal", "S_1", Collections.emptyList(), null);
        session.describe('S', "S_1");
        session.parse("S_2", "select id from sys.cluster", Collections.emptyList());
        session.bind("", "S_2", Collections.emptyList(), null);
        session.describe('S', "S_2");
        session.execute("", 0, new BaseResultReceiver());
        assertThat(session.portals.size(), Matchers.is(2));
        assertThat(session.preparedStatements.size(), Matchers.is(2));
        assertThat(session.pendingExecutions.size(), Matchers.is(1));
        session.close();
        assertThat(session.portals.size(), Matchers.is(0));
        assertThat(session.preparedStatements.size(), Matchers.is(0));
        assertThat(session.pendingExecutions.size(), Matchers.is(0));
    }

    @Test
    public void testDeallocateAllClearsAllPortalsAndPreparedStatements() {
        SQLExecutor sqlExecutor = SQLExecutor.builder(clusterService).build();
        DependencyCarrier executor = Mockito.mock(DependencyCarrier.class);
        Mockito.when(executor.threadPool()).thenReturn(Mockito.mock(ThreadPool.class));
        Session session = new Session(sqlExecutor.analyzer, sqlExecutor.planner, new JobsLogs(() -> false), false, executor, SessionContext.systemSessionContext());
        session.parse("S_1", "select * from sys.cluster;", Collections.emptyList());
        session.bind("Portal", "S_1", Collections.emptyList(), null);
        session.describe('S', "S_1");
        session.parse("S_2", "DEALLOCATE ALL;", Collections.emptyList());
        session.bind("", "S_2", Collections.emptyList(), null);
        session.execute("", 0, new BaseResultReceiver());
        assertThat(session.portals.size(), Matchers.greaterThan(0));
        assertThat(session.preparedStatements.size(), Matchers.is(0));
    }

    @Test
    public void testDeallocatePreparedStatementClearsPreparedStatement() {
        SQLExecutor sqlExecutor = SQLExecutor.builder(clusterService).build();
        DependencyCarrier executor = Mockito.mock(DependencyCarrier.class);
        Mockito.when(executor.threadPool()).thenReturn(Mockito.mock(ThreadPool.class));
        Session session = new Session(sqlExecutor.analyzer, sqlExecutor.planner, new JobsLogs(() -> false), false, executor, SessionContext.systemSessionContext());
        session.parse("test_prep_stmt", "select * from sys.cluster;", Collections.emptyList());
        session.bind("Portal", "test_prep_stmt", Collections.emptyList(), null);
        session.describe('S', "test_prep_stmt");
        session.parse("stmt", "DEALLOCATE test_prep_stmt;", Collections.emptyList());
        session.bind("", "stmt", Collections.emptyList(), null);
        session.execute("", 0, new BaseResultReceiver());
        assertThat(session.portals.size(), Matchers.greaterThan(0));
        assertThat(session.preparedStatements.size(), Matchers.is(1));
        assertThat(session.preparedStatements.get("stmt").query(), Matchers.is("DEALLOCATE test_prep_stmt;"));
    }
}
