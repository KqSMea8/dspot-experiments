/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.sqlobject;


import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.core.rule.H2DatabaseRule;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.statement.BatchChunkSize;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.junit.Rule;
import org.junit.Test;


public class TestBatchingSingleValue {
    @Rule
    public H2DatabaseRule dbRule = new H2DatabaseRule().withPlugin(new SqlObjectPlugin()).withPlugin(new H2DatabasePlugin());

    private Handle handle;

    private TestBatchingSingleValue.SingleValueBatching b;

    @Test
    public void testSingleValueArray() {
        final int[] ids = IntStream.range(0, 10).toArray();
        final int[] values = IntStream.range(50, 60).toArray();
        b.insertValues(ids, values);
        assertThat(b.select()).containsExactly(Arrays.stream(ids).mapToObj(( id) -> new TestBatchingSingleValue.BatchingRow(id, values)).toArray(TestBatchingSingleValue.BatchingRow[]::new));
    }

    @BatchChunkSize(4)
    @RegisterConstructorMapper(TestBatchingSingleValue.BatchingRow.class)
    public interface SingleValueBatching {
        @SqlBatch("insert into batching (id, values) values (:id, :values)")
        int[] insertValues(int[] id, @SingleValue
        int[] values);

        @SqlQuery("select id, values from batching order by id asc")
        List<TestBatchingSingleValue.BatchingRow> select();
    }

    public static class BatchingRow {
        final int id;

        final int[] values;

        public BatchingRow(int id, int[] values) {
            this.id = id;
            this.values = values;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TestBatchingSingleValue.BatchingRow) {
                TestBatchingSingleValue.BatchingRow other = ((TestBatchingSingleValue.BatchingRow) (obj));
                return ((id) == (other.id)) && (Arrays.equals(values, other.values));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (id) ^ (Arrays.hashCode(values));
        }

        @Override
        public String toString() {
            return String.format("%s %s", id, Arrays.toString(values));
        }
    }
}
