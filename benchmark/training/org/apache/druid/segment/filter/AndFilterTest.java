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
package org.apache.druid.segment.filter;


import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import org.apache.druid.data.input.InputRow;
import org.apache.druid.data.input.impl.DimensionsSpec;
import org.apache.druid.data.input.impl.InputRowParser;
import org.apache.druid.data.input.impl.MapInputRowParser;
import org.apache.druid.java.util.common.DateTimes;
import org.apache.druid.java.util.common.Pair;
import org.apache.druid.query.filter.NotDimFilter;
import org.apache.druid.query.filter.SelectorDimFilter;
import org.apache.druid.segment.IndexBuilder;
import org.apache.druid.segment.StorageAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class AndFilterTest extends BaseFilterTest {
    private static final String TIMESTAMP_COLUMN = "timestamp";

    private static final InputRowParser<Map<String, Object>> PARSER = new MapInputRowParser(new org.apache.druid.data.input.impl.TimeAndDimsParseSpec(new org.apache.druid.data.input.impl.TimestampSpec(AndFilterTest.TIMESTAMP_COLUMN, "iso", DateTimes.of("2000")), new DimensionsSpec(null, null, null)));

    private static final List<InputRow> ROWS = ImmutableList.of(AndFilterTest.PARSER.parseBatch(ImmutableMap.of("dim0", "0", "dim1", "0")).get(0), AndFilterTest.PARSER.parseBatch(ImmutableMap.of("dim0", "1", "dim1", "0")).get(0), AndFilterTest.PARSER.parseBatch(ImmutableMap.of("dim0", "2", "dim1", "0")).get(0), AndFilterTest.PARSER.parseBatch(ImmutableMap.of("dim0", "3", "dim1", "0")).get(0), AndFilterTest.PARSER.parseBatch(ImmutableMap.of("dim0", "4", "dim1", "0")).get(0), AndFilterTest.PARSER.parseBatch(ImmutableMap.of("dim0", "5", "dim1", "0")).get(0));

    public AndFilterTest(String testName, IndexBuilder indexBuilder, Function<IndexBuilder, Pair<StorageAdapter, Closeable>> finisher, boolean cnf, boolean optimize) {
        super(testName, AndFilterTest.ROWS, indexBuilder, finisher, cnf, optimize);
    }

    @Test
    public void testAnd() {
        assertFilterMatches(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "0", null), new SelectorDimFilter("dim1", "0", null))), ImmutableList.of("0"));
        assertFilterMatches(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "0", null), new SelectorDimFilter("dim1", "1", null))), ImmutableList.of());
        assertFilterMatches(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "1", null), new SelectorDimFilter("dim1", "0", null))), ImmutableList.of("1"));
        assertFilterMatches(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "1", null), new SelectorDimFilter("dim1", "1", null))), ImmutableList.of());
        assertFilterMatches(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new NotDimFilter(new SelectorDimFilter("dim0", "1", null)), new NotDimFilter(new SelectorDimFilter("dim1", "1", null)))), ImmutableList.of("0", "2", "3", "4", "5"));
        assertFilterMatches(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new NotDimFilter(new SelectorDimFilter("dim0", "0", null)), new NotDimFilter(new SelectorDimFilter("dim1", "0", null)))), ImmutableList.of());
    }

    @Test
    public void testNotAnd() {
        assertFilterMatches(new NotDimFilter(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "0", null), new SelectorDimFilter("dim1", "0", null)))), ImmutableList.of("1", "2", "3", "4", "5"));
        assertFilterMatches(new NotDimFilter(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "0", null), new SelectorDimFilter("dim1", "1", null)))), ImmutableList.of("0", "1", "2", "3", "4", "5"));
        assertFilterMatches(new NotDimFilter(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "1", null), new SelectorDimFilter("dim1", "0", null)))), ImmutableList.of("0", "2", "3", "4", "5"));
        assertFilterMatches(new NotDimFilter(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new SelectorDimFilter("dim0", "1", null), new SelectorDimFilter("dim1", "1", null)))), ImmutableList.of("0", "1", "2", "3", "4", "5"));
        assertFilterMatches(new NotDimFilter(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new NotDimFilter(new SelectorDimFilter("dim0", "1", null)), new NotDimFilter(new SelectorDimFilter("dim1", "1", null))))), ImmutableList.of("1"));
        assertFilterMatches(new NotDimFilter(new org.apache.druid.query.filter.AndDimFilter(ImmutableList.of(new NotDimFilter(new SelectorDimFilter("dim0", "0", null)), new NotDimFilter(new SelectorDimFilter("dim1", "0", null))))), ImmutableList.of("0", "1", "2", "3", "4", "5"));
    }
}
