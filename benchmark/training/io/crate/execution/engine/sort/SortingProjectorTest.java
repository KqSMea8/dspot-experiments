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
package io.crate.execution.engine.sort;


import io.crate.data.BatchIterator;
import io.crate.data.Bucket;
import io.crate.data.Row;
import io.crate.test.integration.CrateUnitTest;
import io.crate.testing.TestingBatchIterators;
import io.crate.testing.TestingHelpers;
import io.crate.testing.TestingRowConsumer;
import org.hamcrest.core.Is;
import org.junit.Test;


public class SortingProjectorTest extends CrateUnitTest {
    private TestingRowConsumer consumer = new TestingRowConsumer();

    @Test
    public void testOrderBy() throws Exception {
        SortingProjector projector = createProjector(2, 0);
        BatchIterator batchIterator = projector.apply(TestingBatchIterators.range(1, 11));
        consumer.accept(batchIterator, null);
        Bucket rows = consumer.getBucket();
        assertThat(rows.size(), Is.is(10));
        int iterateLength = 1;
        for (Row row : rows) {
            assertThat(row, TestingHelpers.isRow((iterateLength++), true));
        }
    }

    @Test
    public void testOrderByWithOffset() throws Exception {
        SortingProjector projector = createProjector(2, 5);
        BatchIterator batchIterator = projector.apply(TestingBatchIterators.range(1, 11));
        consumer.accept(batchIterator, null);
        Bucket rows = consumer.getBucket();
        assertThat(rows.size(), Is.is(5));
        int iterateLength = 6;
        for (Row row : rows) {
            assertThat(row, TestingHelpers.isRow((iterateLength++), true));
        }
    }

    @Test
    public void testInvalidOffset() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("invalid offset -1");
        new SortingProjector(null, null, 2, null, (-1));
    }
}
