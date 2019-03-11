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
package io.crate.integrationtests;


import ESIntegTestCase.ClusterScope;
import io.crate.action.sql.SQLActionException;
import org.hamcrest.Matchers;
import org.junit.Test;


@ClusterScope(numClientNodes = 0)
public class SysNodesITest extends SQLTransportIntegrationTest {
    @Test
    public void testNoMatchingNode() throws Exception {
        execute("select id, name, hostname from sys.nodes where id = 'does-not-exist'");
        assertThat(response.rowCount(), Matchers.is(0L));
    }

    @Test
    public void testScalarEvaluatesInErrorOnSysNodes() throws Exception {
        expectedException.expect(SQLActionException.class);
        expectedException.expectMessage(" / by zero");
        execute("select 1/0 from sys.nodes");
    }

    @Test
    public void testRestUrl() throws Exception {
        execute("select rest_url from sys.nodes");
        assertThat(((String) (response.rows()[0][0])), Matchers.startsWith("127.0.0.1:"));
    }
}
