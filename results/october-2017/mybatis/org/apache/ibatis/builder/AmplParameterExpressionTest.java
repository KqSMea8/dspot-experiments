/**
 * Copyright 2009-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.builder;


public class AmplParameterExpressionTest {
    @org.junit.Rule
    public org.junit.rules.ExpectedException expectedException = org.junit.rules.ExpectedException.none();

    @org.junit.Test
    public void simpleProperty() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("id");
        org.junit.Assert.assertEquals(1, result.size());
        org.junit.Assert.assertEquals("id", result.get("property"));
    }

    public void propertyWithSpacesInside() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression(" with spaces ");
        org.junit.Assert.assertEquals(1, result.size());
        org.junit.Assert.assertEquals("with spaces", result.get("property"));
    }

    @org.junit.Test
    public void simplePropertyWithOldStyleJdbcType() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("id:VARCHAR");
        org.junit.Assert.assertEquals(2, result.size());
        org.junit.Assert.assertEquals("id", result.get("property"));
        org.junit.Assert.assertEquals("VARCHAR", result.get("jdbcType"));
    }

    @org.junit.Test
    public void oldStyleJdbcTypeWithExtraWhitespaces() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression(" id :  VARCHAR ");
        org.junit.Assert.assertEquals(2, result.size());
        org.junit.Assert.assertEquals("id", result.get("property"));
        org.junit.Assert.assertEquals("VARCHAR", result.get("jdbcType"));
    }

    @org.junit.Test
    public void expressionWithOldStyleJdbcType() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("(id.toString()):VARCHAR");
        org.junit.Assert.assertEquals(2, result.size());
        org.junit.Assert.assertEquals("id.toString()", result.get("expression"));
        org.junit.Assert.assertEquals("VARCHAR", result.get("jdbcType"));
    }

    @org.junit.Test
    public void simplePropertyWithOneAttribute() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("id,name=value");
        org.junit.Assert.assertEquals(2, result.size());
        org.junit.Assert.assertEquals("id", result.get("property"));
        org.junit.Assert.assertEquals("value", result.get("name"));
    }

    @org.junit.Test
    public void expressionWithOneAttribute() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("(id.toString()),name=value");
        org.junit.Assert.assertEquals(2, result.size());
        org.junit.Assert.assertEquals("id.toString()", result.get("expression"));
        org.junit.Assert.assertEquals("value", result.get("name"));
    }

    @org.junit.Test
    public void simplePropertyWithManyAttributes() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("id, attr1=val1, attr2=val2, attr3=val3");
        org.junit.Assert.assertEquals(4, result.size());
        org.junit.Assert.assertEquals("id", result.get("property"));
        org.junit.Assert.assertEquals("val1", result.get("attr1"));
        org.junit.Assert.assertEquals("val2", result.get("attr2"));
        org.junit.Assert.assertEquals("val3", result.get("attr3"));
    }

    @org.junit.Test
    public void expressionWithManyAttributes() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("(id.toString()), attr1=val1, attr2=val2, attr3=val3");
        org.junit.Assert.assertEquals(4, result.size());
        org.junit.Assert.assertEquals("id.toString()", result.get("expression"));
        org.junit.Assert.assertEquals("val1", result.get("attr1"));
        org.junit.Assert.assertEquals("val2", result.get("attr2"));
        org.junit.Assert.assertEquals("val3", result.get("attr3"));
    }

    @org.junit.Test
    public void simplePropertyWithOldStyleJdbcTypeAndAttributes() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("id:VARCHAR, attr1=val1, attr2=val2");
        org.junit.Assert.assertEquals(4, result.size());
        org.junit.Assert.assertEquals("id", result.get("property"));
        org.junit.Assert.assertEquals("VARCHAR", result.get("jdbcType"));
        org.junit.Assert.assertEquals("val1", result.get("attr1"));
        org.junit.Assert.assertEquals("val2", result.get("attr2"));
    }

    @org.junit.Test
    public void simplePropertyWithSpaceAndManyAttributes() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("user name, attr1=val1, attr2=val2, attr3=val3");
        org.junit.Assert.assertEquals(4, result.size());
        org.junit.Assert.assertEquals("user name", result.get("property"));
        org.junit.Assert.assertEquals("val1", result.get("attr1"));
        org.junit.Assert.assertEquals("val2", result.get("attr2"));
        org.junit.Assert.assertEquals("val3", result.get("attr3"));
    }

    @org.junit.Test
    public void shouldIgnoreLeadingAndTrailingSpaces() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression(" id , jdbcType =  VARCHAR,  attr1 = val1 ,  attr2 = val2 ");
        org.junit.Assert.assertEquals(4, result.size());
        org.junit.Assert.assertEquals("id", result.get("property"));
        org.junit.Assert.assertEquals("VARCHAR", result.get("jdbcType"));
        org.junit.Assert.assertEquals("val1", result.get("attr1"));
        org.junit.Assert.assertEquals("val2", result.get("attr2"));
    }

    @org.junit.Test
    public void invalidOldJdbcTypeFormat() {
        expectedException.expect(org.apache.ibatis.builder.BuilderException.class);
        expectedException.expectMessage(org.hamcrest.core.Is.is("Parsing error in {id:} in position 3"));
        new org.apache.ibatis.builder.ParameterExpression("id:");
    }

    @org.junit.Test
    public void invalidJdbcTypeOptUsingExpression() {
        expectedException.expect(org.apache.ibatis.builder.BuilderException.class);
        expectedException.expectMessage(org.hamcrest.core.Is.is("Parsing error in {(expression)+} in position 12"));
        new org.apache.ibatis.builder.ParameterExpression("(expression)+");
    }

    /* amplification of org.apache.ibatis.builder.ParameterExpressionTest#simplePropertyWithOneAttribute */
    /* amplification of org.apache.ibatis.builder.ParameterExpressionTest#simplePropertyWithOneAttribute_literalMutationString63414 */
    @org.junit.Test(timeout = 10000)
    public void simplePropertyWithOneAttribute_literalMutationString63414_literalMutationString63500() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("!67l5Y0.]pp<7");
        // AssertGenerator create local variable with return value of invocation
        int o_simplePropertyWithOneAttribute_literalMutationString63414__3 = result.size();
        // AssertGenerator create local variable with return value of invocation
        java.lang.String o_simplePropertyWithOneAttribute_literalMutationString63414__4 = result.get("property");
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("!67l5Y0.]pp<7", o_simplePropertyWithOneAttribute_literalMutationString63414__4);
        // AssertGenerator create local variable with return value of invocation
        java.lang.String o_simplePropertyWithOneAttribute_literalMutationString63414__5 = result.get("name");
        // AssertGenerator add assertion
        org.junit.Assert.assertNull(o_simplePropertyWithOneAttribute_literalMutationString63414__5);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("!67l5Y0.]pp<7", o_simplePropertyWithOneAttribute_literalMutationString63414__4);
    }

    /* amplification of org.apache.ibatis.builder.ParameterExpressionTest#simplePropertyWithOneAttribute */
    /* amplification of org.apache.ibatis.builder.ParameterExpressionTest#simplePropertyWithOneAttribute_add63431 */
    /* amplification of org.apache.ibatis.builder.ParameterExpressionTest#simplePropertyWithOneAttribute_add63431_literalMutationString63859 */
    @org.junit.Test(timeout = 10000)
    public void simplePropertyWithOneAttribute_add63431_literalMutationString63859_literalMutationString66961() {
        java.util.Map<java.lang.String, java.lang.String> result = new org.apache.ibatis.builder.ParameterExpression("!MUr8K:QR(M8<");
        // AssertGenerator create local variable with return value of invocation
        int o_simplePropertyWithOneAttribute_add63431__3 = // MethodCallAdder
        result.size();
        // AssertGenerator create local variable with return value of invocation
        int o_simplePropertyWithOneAttribute_add63431__5 = result.size();
        // AssertGenerator create local variable with return value of invocation
        java.lang.String o_simplePropertyWithOneAttribute_add63431__6 = result.get("property");
        // AssertGenerator create local variable with return value of invocation
        java.lang.String o_simplePropertyWithOneAttribute_add63431__7 = result.get("");
        // AssertGenerator add assertion
        org.junit.Assert.assertNull(o_simplePropertyWithOneAttribute_add63431__7);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("!MUr8K", o_simplePropertyWithOneAttribute_add63431__6);
    }
}

