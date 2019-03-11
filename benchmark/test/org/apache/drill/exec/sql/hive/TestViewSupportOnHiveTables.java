/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.sql.hive;


import org.apache.drill.categories.HiveStorageTest;
import org.apache.drill.categories.SlowTest;
import org.apache.drill.exec.sql.TestBaseViewSupport;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category({ SlowTest.class, HiveStorageTest.class })
public class TestViewSupportOnHiveTables extends TestBaseViewSupport {
    @Test
    public void viewWithStarInDef_StarInQuery() throws Exception {
        testViewHelper(DFS_TMP_SCHEMA, null, "SELECT * FROM hive.kv", "SELECT * FROM TEST_SCHEMA.TEST_VIEW_NAME LIMIT 1", baselineColumns("key", "value"), baselineRows(row(1, " key_1")));
    }

    @Test
    public void viewWithStarInDef_SelectFieldsInQuery1() throws Exception {
        testViewHelper(DFS_TMP_SCHEMA, null, "SELECT * FROM hive.kv", "SELECT key, `value` FROM TEST_SCHEMA.TEST_VIEW_NAME LIMIT 1", baselineColumns("key", "value"), baselineRows(row(1, " key_1")));
    }

    @Test
    public void viewWithStarInDef_SelectFieldsInQuery2() throws Exception {
        testViewHelper(DFS_TMP_SCHEMA, null, "SELECT * FROM hive.kv", "SELECT `value` FROM TEST_SCHEMA.TEST_VIEW_NAME LIMIT 1", baselineColumns("value"), baselineRows(row(" key_1")));
    }

    @Test
    public void viewWithSelectFieldsInDef_StarInQuery() throws Exception {
        testViewHelper(DFS_TMP_SCHEMA, null, "SELECT key, `value` FROM hive.kv", "SELECT * FROM TEST_SCHEMA.TEST_VIEW_NAME LIMIT 1", baselineColumns("key", "value"), baselineRows(row(1, " key_1")));
    }

    @Test
    public void viewWithSelectFieldsInDef_SelectFieldsInQuery() throws Exception {
        testViewHelper(DFS_TMP_SCHEMA, null, "SELECT key, `value` FROM hive.kv", "SELECT key, `value` FROM TEST_SCHEMA.TEST_VIEW_NAME LIMIT 1", baselineColumns("key", "value"), baselineRows(row(1, " key_1")));
    }

    @Test
    public void testInfoSchemaWithHiveView() throws Exception {
        testBuilder().optionSettingQueriesForTestQuery("USE hive.`default`").sqlQuery("SELECT * FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = 'hive_view'").unOrdered().baselineColumns("TABLE_CATALOG", "TABLE_SCHEMA", "TABLE_NAME", "VIEW_DEFINITION").baselineValues("DRILL", "hive.default", "hive_view", "SELECT `kv`.`key`, `kv`.`value` FROM `default`.`kv`").go();
    }
}
