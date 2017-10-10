/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.clearspring.analytics.hash;


/**
 *
 *
 * @author epollan
 */
public class TestMurmurHashAmpl {
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload() {
        java.lang.String input = "hashthis";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hashthis", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-8896273065425798843L, ((long) (o_testHash64ByteArrayOverload__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-8896273065425798843L, ((long) (o_testHash64ByteArrayOverload__6)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hashthis", input);
    }

    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload() {
        java.lang.String input = "hashthis";
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-1974946086, ((int) (o_testHashByteArrayOverload__6)));
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-1974946086, ((int) (o_testHashByteArrayOverload__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-1974946086, ((int) (o_testHashByteArrayOverload__6)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hashthis", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString6() {
        java.lang.String input = "hshthis";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hshthis", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(6901914564845240700L, ((long) (o_testHash64ByteArrayOverload_literalMutationString6__6)));
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(6901914564845240700L, ((long) (o_testHash64ByteArrayOverload_literalMutationString6__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hshthis", input);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(6901914564845240700L, ((long) (o_testHash64ByteArrayOverload_literalMutationString6__6)));
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString2() {
        java.lang.String input = "MurmurHash.hash64(Object) given a byte[] did not match MurmurHash.hash64(String)";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("MurmurHash.hash64(Object) given a byte[] did not match MurmurHash.hash64(String)", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-7065322190441338459L, ((long) (o_testHash64ByteArrayOverload_literalMutationString2__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("MurmurHash.hash64(Object) given a byte[] did not match MurmurHash.hash64(String)", input);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-7065322190441338459L, ((long) (o_testHash64ByteArrayOverload_literalMutationString2__6)));
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString3() {
        java.lang.String input = "hash}this";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hash}this", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString3__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-4410106181081413790L, ((long) (o_testHash64ByteArrayOverload_literalMutationString3__6)));
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString3__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-4410106181081413790L, ((long) (o_testHash64ByteArrayOverload_literalMutationString3__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hash}this", input);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-4410106181081413790L, ((long) (o_testHash64ByteArrayOverload_literalMutationString3__6)));
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString4() {
        java.lang.String input = "h*shthis";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-7852005312626181413L, ((long) (o_testHash64ByteArrayOverload_literalMutationString4__6)));
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-7852005312626181413L, ((long) (o_testHash64ByteArrayOverload_literalMutationString4__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-7852005312626181413L, ((long) (o_testHash64ByteArrayOverload_literalMutationString4__6)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("h*shthis", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString3 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString3_literalMutationString35() {
        java.lang.String input = "hash}t&is";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString3__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString3__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hash}t&is", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString2 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString2_literalMutationString32() {
        java.lang.String input = "hello world";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hello world", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString2 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString2_literalMutationString31() {
        java.lang.String input = "_,y(q2 5[gpbL[{$QV5:Wz2[|+mr6#-VtX(r!Fs2l>UgIvC=TU&zgYc TM1`_8;0L`A=SO/woO!OKS@R";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("_,y(q2 5[gpbL[{$QV5:Wz2[|+mr6#-VtX(r!Fs2l>UgIvC=TU&zgYc TM1`_8;0L`A=SO/woO!OKS@R", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString5 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString5_literalMutationString46() {
        java.lang.String input = "";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString5__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString5__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString5 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString5_literalMutationString47() {
        java.lang.String input = "GdhscbS";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString5__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString5__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("GdhscbS", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString6 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString6_literalMutationString57() {
        java.lang.String input = "hshtis";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hshtis", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hshtis", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString2 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString2_literalMutationString32 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString2_literalMutationString32_literalMutationString192() {
        java.lang.String input = "h4llo world";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("h4llo world", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4_literalMutationString42 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString4_literalMutationString42_literalMutationString249() {
        java.lang.String input = "+DN-eV8";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("+DN-eV8", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4_literalMutationString43 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString4_literalMutationString43_literalMutationString252() {
        java.lang.String input = "MurmurHash.hash64(Object) given a byte[] did not match MurmurHash.hash64(String)";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("MurmurHash.hash64(Object) given a byte[] did not match MurmurHash.hash64(String)", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("MurmurHash.hash64(Object) given a byte[] did not match MurmurHash.hash64(String)", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString6 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString6_literalMutationString57 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString6_literalMutationString57_literalMutationString329() {
        java.lang.String input = "UhLoAf";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("UhLoAf", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4_literalMutationString43 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString4_literalMutationString43_literalMutationString251() {
        java.lang.String input = "?xw0]W#n";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("?xw0]W#n", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("?xw0]W#n", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString2 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString2_literalMutationString32 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString2_literalMutationString32_literalMutationString195() {
        java.lang.String input = "hello wor(ld";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hello wor(ld", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString2__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hello wor(ld", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4_literalMutationString43 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString4_literalMutationString43_literalMutationString250() {
        java.lang.String input = "";
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString6 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString6_literalMutationString57 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString6_literalMutationString57_literalMutationString331() {
        java.lang.String input = "hhtis";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hhtis", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString6__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hhtis", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHash64ByteArrayOverload_literalMutationString4_literalMutationString44 */
    @org.junit.Test(timeout = 10000)
    public void testHash64ByteArrayOverload_literalMutationString4_literalMutationString44_literalMutationString260() {
        java.lang.String input = "@IY:Wo-9M";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("@IY:Wo-9M", input);
        byte[] inputBytes = input.getBytes();
        long hashOfString = com.clearspring.analytics.hash.MurmurHash.hash64(input);
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__6 = com.clearspring.analytics.hash.MurmurHash.hash64(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        long o_testHash64ByteArrayOverload_literalMutationString4__8 = com.clearspring.analytics.hash.MurmurHash.hash64(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("@IY:Wo-9M", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString867() {
        java.lang.String input = "hashhis";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hashhis", input);
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString867__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString867__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-590652159, ((int) (o_testHashByteArrayOverload_literalMutationString867__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hashhis", input);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-590652159, ((int) (o_testHashByteArrayOverload_literalMutationString867__6)));
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString868() {
        java.lang.String input = "hashAthis";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hashAthis", input);
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString868__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(426960432, ((int) (o_testHashByteArrayOverload_literalMutationString868__6)));
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString868__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(426960432, ((int) (o_testHashByteArrayOverload_literalMutationString868__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hashAthis", input);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(426960432, ((int) (o_testHashByteArrayOverload_literalMutationString868__6)));
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString869() {
        java.lang.String input = "^(qL7}kh";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("^(qL7}kh", input);
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString869__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString869__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-584577238, ((int) (o_testHashByteArrayOverload_literalMutationString869__8)));
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("^(qL7}kh", input);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals(-584577238, ((int) (o_testHashByteArrayOverload_literalMutationString869__6)));
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString866 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString866_literalMutationString897() {
        java.lang.String input = "MurmurHashhash64(Object) given a byte[] did not match MurmurHash.hash64(String)";
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString866__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString866__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("MurmurHashhash64(Object) given a byte[] did not match MurmurHash.hash64(String)", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString865 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString865_literalMutationString891() {
        java.lang.String input = "?";
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString865__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString865__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("?", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString866 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString866_literalMutationString893() {
        java.lang.String input = "ySysP>6W.t0C-?9AC*$S oY.>c^U!$Cz2lvLY3Pe#L360:}[gYFUICnc)SU7EvLBHp9HIW?9U-1%h+1!";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("ySysP>6W.t0C-?9AC*$S oY.>c^U!$Cz2lvLY3Pe#L360:}[gYFUICnc)SU7EvLBHp9HIW?9U-1%h+1!", input);
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString866__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString866__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("ySysP>6W.t0C-?9AC*$S oY.>c^U!$Cz2lvLY3Pe#L360:}[gYFUICnc)SU7EvLBHp9HIW?9U-1%h+1!", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString866 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString866_literalMutationString892() {
        java.lang.String input = "";
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString866__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString866__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString869 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString869_literalMutationString910 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString869_literalMutationString910_literalMutationString1134() {
        java.lang.String input = "U";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("U", input);
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString869__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString869__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("U", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString868 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString868_literalMutationString906 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString868_literalMutationString906_literalMutationString1109() {
        java.lang.String input = "";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("", input);
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString868__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString868__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString867 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString867_literalMutationString901 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString867_literalMutationString901_literalMutationString1085() {
        java.lang.String input = "hello world";
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString867__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString867__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("hello world", input);
    }

    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString868 */
    /* amplification of com.clearspring.analytics.hash.TestMurmurHash#testHashByteArrayOverload_literalMutationString868_literalMutationString904 */
    @org.junit.Test(timeout = 10000)
    public void testHashByteArrayOverload_literalMutationString868_literalMutationString904_literalMutationString1102() {
        java.lang.String input = "O!ZP#i,s";
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("O!ZP#i,s", input);
        byte[] inputBytes = input.getBytes();
        int hashOfString = com.clearspring.analytics.hash.MurmurHash.hash(input);
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString868__6 = com.clearspring.analytics.hash.MurmurHash.hash(inputBytes);
        java.lang.Object bytesAsObject = inputBytes;
        // AssertGenerator create local variable with return value of invocation
        int o_testHashByteArrayOverload_literalMutationString868__8 = com.clearspring.analytics.hash.MurmurHash.hash(bytesAsObject);
        // AssertGenerator add assertion
        org.junit.Assert.assertEquals("O!ZP#i,s", input);
    }
}

