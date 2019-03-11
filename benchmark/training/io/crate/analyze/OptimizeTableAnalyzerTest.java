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
package io.crate.analyze;


import io.crate.exceptions.OperationOnInaccessibleRelationException;
import io.crate.exceptions.RelationUnknown;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;


public class OptimizeTableAnalyzerTest extends CrateDummyClusterServiceUnitTest {
    private SQLExecutor e;

    @Test
    public void testOptimizeSystemTable() throws Exception {
        expectedException.expect(OperationOnInaccessibleRelationException.class);
        expectedException.expectMessage(("The relation \"sys.shards\" doesn\'t support or allow OPTIMIZE " + "operations, as it is read-only."));
        e.analyze("OPTIMIZE TABLE sys.shards");
    }

    @Test
    public void testOptimizeTable() throws Exception {
        OptimizeTableAnalyzedStatement analysis = e.analyze("OPTIMIZE TABLE users");
        assertThat(analysis.indexNames().size(), Matchers.is(1));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem("users"));
    }

    @Test
    public void testOptimizeBlobTable() throws Exception {
        OptimizeTableAnalyzedStatement analysis = e.analyze("OPTIMIZE TABLE blob.blobs");
        assertThat(analysis.indexNames().size(), Matchers.is(1));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem(".blob_blobs"));
    }

    @Test
    public void testOptimizeTableWithParams() throws Exception {
        OptimizeTableAnalyzedStatement analysis = e.analyze("OPTIMIZE TABLE users WITH (max_num_segments=2)");
        assertThat(analysis.indexNames().size(), Matchers.is(1));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem("users"));
        assertThat(OptimizeTableAnalyzer.MAX_NUM_SEGMENTS.get(analysis.settings()), Matchers.is(2));
        analysis = e.analyze("OPTIMIZE TABLE users WITH (only_expunge_deletes=true)");
        assertThat(analysis.indexNames().size(), Matchers.is(1));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem("users"));
        assertThat(OptimizeTableAnalyzer.ONLY_EXPUNGE_DELETES.get(analysis.settings()), Matchers.is(Boolean.TRUE));
        analysis = e.analyze("OPTIMIZE TABLE users WITH (flush=false)");
        assertThat(analysis.indexNames().size(), Matchers.is(1));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem("users"));
        assertThat(OptimizeTableAnalyzer.FLUSH.get(analysis.settings()), Matchers.is(Boolean.FALSE));
        analysis = e.analyze("OPTIMIZE TABLE users WITH (upgrade_segments=true)");
        assertThat(analysis.indexNames().size(), Matchers.is(1));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem("users"));
        assertThat(OptimizeTableAnalyzer.UPGRADE_SEGMENTS.get(analysis.settings()), Matchers.is(Boolean.TRUE));
    }

    @Test
    public void testOptimizeTableWithInvalidParamName() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("setting 'invalidparam' not supported");
        e.analyze("OPTIMIZE TABLE users WITH (invalidParam=123)");
    }

    @Test
    public void testOptimizeTableWithUpgradeSegmentsAndOtherParam() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("cannot use other parameters if upgrade_segments is set to true");
        e.analyze("OPTIMIZE TABLE users WITH (flush=false, upgrade_segments=true)");
    }

    @Test
    public void testOptimizePartition() throws Exception {
        OptimizeTableAnalyzedStatement analysis = e.analyze("OPTIMIZE TABLE parted PARTITION (date=1395874800000)");
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem(".partitioned.parted.04732cpp6ks3ed1o60o30c1g"));
    }

    @Test
    public void testOptimizePartitionedTableNullPartition() throws Exception {
        OptimizeTableAnalyzedStatement analysis = e.analyze("OPTIMIZE TABLE parted PARTITION (date=null)");
        assertThat(analysis.indexNames(), Matchers.contains(Matchers.hasToString(".partitioned.parted.0400")));
    }

    @Test
    public void testOptimizePartitionWithParams() throws Exception {
        OptimizeTableAnalyzedStatement analysis = e.analyze(("OPTIMIZE TABLE parted PARTITION (date=1395874800000) " + "WITH (only_expunge_deletes=true)"));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem(".partitioned.parted.04732cpp6ks3ed1o60o30c1g"));
    }

    @Test
    public void testOptimizeMultipleTables() throws Exception {
        OptimizeTableAnalyzedStatement analysis = e.analyze("OPTIMIZE TABLE parted, users");
        assertThat(analysis.indexNames().size(), Matchers.is(4));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem(".partitioned.parted.04732cpp6ks3ed1o60o30c1g"));
        assertThat(analysis.indexNames(), IsCollectionContaining.hasItem("users"));
    }

    @Test
    public void testOptimizeMultipleTablesUnknown() throws Exception {
        expectedException.expect(RelationUnknown.class);
        expectedException.expectMessage("Relation 'foo' unknown");
        e.analyze("OPTIMIZE TABLE parted, foo, bar");
    }

    @Test
    public void testOptimizeInvalidPartitioned() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("\"invalid_column\" is no known partition column");
        e.analyze("OPTIMIZE TABLE parted PARTITION (invalid_column='hddsGNJHSGFEFZ?')");
    }

    @Test
    public void testOptimizeNonPartitioned() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("table 'doc.users' is not partitioned");
        e.analyze("OPTIMIZE TABLE users PARTITION (foo='n')");
    }

    @Test
    public void testOptimizeSysPartitioned() throws Exception {
        expectedException.expect(OperationOnInaccessibleRelationException.class);
        expectedException.expectMessage(("The relation \"sys.shards\" doesn\'t support or allow OPTIMIZE " + "operations, as it is read-only."));
        e.analyze("OPTIMIZE TABLE sys.shards PARTITION (id='n')");
    }
}
