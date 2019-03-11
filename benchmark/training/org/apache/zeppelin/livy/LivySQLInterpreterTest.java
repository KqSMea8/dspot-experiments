/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zeppelin.livy;


import java.util.List;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test for LivySQLInterpreter.
 */
public class LivySQLInterpreterTest {
    private LivySparkSQLInterpreter sqlInterpreter;

    @Test
    public void testHttpHeaders() {
        Assert.assertEquals(1, sqlInterpreter.getCustomHeaders().size());
        Assert.assertTrue(sqlInterpreter.getCustomHeaders().get("HEADER_1").startsWith("VALUE_1_"));
        Assert.assertNotEquals("VALUE_1_${HOME}", sqlInterpreter.getCustomHeaders().get("HEADER_1"));
    }

    @Test
    public void testParseSQLOutput() {
        // Empty sql output
        // +---+---+
        // |  a|  b|
        // +---+---+
        // +---+---+
        List<String> rows = sqlInterpreter.parseSQLOutput(("+---+---+\n" + (("|  a|  b|\n" + "+---+---+\n") + "+---+---+")));
        Assert.assertEquals(1, rows.size());
        Assert.assertEquals("a\tb", rows.get(0));
        // sql output with 2 rows
        // +---+---+
        // |  a|  b|
        // +---+---+
        // |  1| 1a|
        // |  2| 2b|
        // +---+---+
        rows = sqlInterpreter.parseSQLOutput(("+---+---+\n" + (((("|  a|  b|\n" + "+---+---+\n") + "|  1| 1a|\n") + "|  2| 2b|\n") + "+---+---+")));
        Assert.assertEquals(3, rows.size());
        Assert.assertEquals("a\tb", rows.get(0));
        Assert.assertEquals("1\t1a", rows.get(1));
        Assert.assertEquals("2\t2b", rows.get(2));
        // sql output with 3 rows and showing "only showing top 3 rows"
        // +---+---+
        // |  a|  b|
        // +---+---+
        // |  1| 1a|
        // |  2| 2b|
        // |  3| 3c|
        // +---+---+
        // only showing top 3 rows
        rows = sqlInterpreter.parseSQLOutput(("+---+---+\n" + (((((("|  a|  b|\n" + "+---+---+\n") + "|  1| 1a|\n") + "|  2| 2b|\n") + "|  3| 3c|\n") + "+---+---+\n") + "only showing top 3 rows")));
        Assert.assertEquals(4, rows.size());
        Assert.assertEquals("a\tb", rows.get(0));
        Assert.assertEquals("1\t1a", rows.get(1));
        Assert.assertEquals("2\t2b", rows.get(2));
        Assert.assertEquals("3\t3c", rows.get(3));
        // sql output with 1 rows and showing "only showing top 1 rows"
        // +---+
        // |  a|
        // +---+
        // |  1|
        // +---+
        // only showing top 1 rows
        rows = sqlInterpreter.parseSQLOutput(("+---+\n" + (((("|  a|\n" + "+---+\n") + "|  1|\n") + "+---+\n") + "only showing top 1 rows")));
        Assert.assertEquals(2, rows.size());
        Assert.assertEquals("a", rows.get(0));
        Assert.assertEquals("1", rows.get(1));
        // sql output with 3 rows, 3 columns, showing "only showing top 3 rows" with a line break in
        // the data
        // +---+---+---+
        // |  a|  b|  c|
        // +---+---+---+
        // | 1a| 1b| 1c|
        // | 2a| 2
        // b| 2c|
        // | 3a| 3b| 3c|
        // +---+---+---+
        // only showing top 3 rows
        rows = sqlInterpreter.parseSQLOutput(("+---+----+---+\n" + (((((("|  a|   b|  c|\n" + "+---+----+---+\n") + "| 1a|  1b| 1c|\n") + "| 2a| 2\nb| 2c|\n") + "| 3a|  3b| 3c|\n") + "+---+---+---+\n") + "only showing top 3 rows")));
        Assert.assertEquals(4, rows.size());
        Assert.assertEquals("a\tb\tc", rows.get(0));
        Assert.assertEquals("1a\t1b\t1c", rows.get(1));
        Assert.assertEquals("2a\t2\\nb\t2c", rows.get(2));
        Assert.assertEquals("3a\t3b\t3c", rows.get(3));
        // sql output with 2 rows and one containing a tab
        // +---+---+
        // |  a|  b|
        // +---+---+
        // |  1| \ta|
        // |  2| 2b|
        // +---+---+
        rows = sqlInterpreter.parseSQLOutput(("+---+---+\n" + (((("|  a|  b|\n" + "+---+---+\n") + "|  1| \ta|\n") + "|  2| 2b|\n") + "+---+---+")));
        Assert.assertEquals(3, rows.size());
        Assert.assertEquals("a\tb", rows.get(0));
        Assert.assertEquals("1\t\\ta", rows.get(1));
        Assert.assertEquals("2\t2b", rows.get(2));
    }
}
