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


import DataTypes.UNDEFINED;
import io.crate.expression.symbol.Literal;
import io.crate.test.integration.CrateDummyClusterServiceUnitTest;
import io.crate.testing.SQLExecutor;
import org.hamcrest.CoreMatchers;
import org.junit.Test;


public class UserDDLAnalyzerTest extends CrateDummyClusterServiceUnitTest {
    private SQLExecutor e;

    @Test
    public void testCreateUserSimple() {
        CreateUserAnalyzedStatement analysis = e.analyze("CREATE USER ROOT");
        assertThat(analysis.userName(), CoreMatchers.is("root"));
        analysis = e.analyze("CREATE USER \"ROOT\"");
        assertThat(analysis.userName(), CoreMatchers.is("ROOT"));
    }

    @Test
    public void testDropUserSimple() {
        DropUserAnalyzedStatement analysis = e.analyze("DROP USER ROOT");
        assertThat(analysis.userName(), CoreMatchers.is("root"));
        analysis = e.analyze("DROP USER \"ROOT\"");
        assertThat(analysis.userName(), CoreMatchers.is("ROOT"));
    }

    @Test
    public void testDropUserIfExists() {
        DropUserAnalyzedStatement analysis = e.analyze("DROP USER IF EXISTS ROOT");
        assertThat(analysis.userName(), CoreMatchers.is("root"));
        assertThat(analysis.ifExists(), CoreMatchers.is(true));
    }

    @Test
    public void testCreateUserWithPassword() throws Exception {
        CreateUserAnalyzedStatement analysis = e.analyze("CREATE USER ROOT WITH (PASSWORD = 'ROOT')");
        assertThat(analysis.userName(), CoreMatchers.is("root"));
        assertThat(analysis.properties().get("password"), CoreMatchers.is(Literal.of("ROOT")));
    }

    @Test
    public void testCreateUserWithPasswordIsStringLiteral() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("Cannot resolve field references");
        CreateUserAnalyzedStatement analysis = e.analyze("CREATE USER ROO WITH (PASSWORD = NO_STRING)");
    }

    @Test
    public void testAlterUserWithPassword() throws Exception {
        AlterUserAnalyzedStatement analysis = e.analyze("ALTER USER ROOT SET (PASSWORD = 'ROOT')");
        assertThat(analysis.userName(), CoreMatchers.is("root"));
        assertThat(analysis.properties().get("password"), CoreMatchers.is(Literal.of("ROOT")));
    }

    @Test
    public void testAlterUserResetPassword() throws Exception {
        AlterUserAnalyzedStatement analysis = e.analyze("ALTER USER ROOT SET (PASSWORD = NULL)");
        assertThat(analysis.userName(), CoreMatchers.is("root"));
        assertThat(analysis.properties().get("password"), CoreMatchers.is(Literal.of(UNDEFINED, null)));
    }
}
