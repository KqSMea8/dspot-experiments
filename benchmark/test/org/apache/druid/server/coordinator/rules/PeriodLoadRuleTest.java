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
package org.apache.druid.server.coordinator.rules;


import DataSegment.Builder;
import DruidServer.DEFAULT_NUM_REPLICANTS;
import DruidServer.DEFAULT_TIER;
import PeriodLoadRule.DEFAULT_INCLUDE_FUTURE;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.druid.client.DruidServer;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.apache.druid.java.util.common.DateTimes;
import org.apache.druid.java.util.common.Intervals;
import org.apache.druid.timeline.DataSegment;
import org.apache.druid.timeline.partition.NoneShardSpec;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

import static PeriodLoadRule.DEFAULT_INCLUDE_FUTURE;


/**
 *
 */
public class PeriodLoadRuleTest {
    private static final Builder builder = DataSegment.builder().dataSource("test").version(DateTimes.nowUtc().toString()).shardSpec(NoneShardSpec.instance());

    @Test
    public void testAppliesToAll() {
        DateTime now = DateTimes.of("2013-01-01");
        PeriodLoadRule rule = new PeriodLoadRule(new Period("P5000Y"), false, ImmutableMap.of("", 0));
        Assert.assertTrue(rule.appliesTo(PeriodLoadRuleTest.builder.interval(Intervals.of("2012-01-01/2012-12-31")).build(), now));
        Assert.assertTrue(rule.appliesTo(PeriodLoadRuleTest.builder.interval(Intervals.of("1000-01-01/2012-12-31")).build(), now));
        Assert.assertTrue(rule.appliesTo(PeriodLoadRuleTest.builder.interval(Intervals.of("0500-01-01/2100-12-31")).build(), now));
    }

    @Test
    public void testAppliesToPeriod() {
        DateTime now = DateTimes.of("2012-12-31T01:00:00");
        PeriodLoadRule rule = new PeriodLoadRule(new Period("P1M"), false, ImmutableMap.of("", 0));
        Assert.assertTrue(rule.appliesTo(PeriodLoadRuleTest.builder.interval(new org.joda.time.Interval(now.minusWeeks(1), now)).build(), now));
        Assert.assertTrue(rule.appliesTo(PeriodLoadRuleTest.builder.interval(new org.joda.time.Interval(now.minusDays(1), now.plusDays(1))).build(), now));
        Assert.assertFalse(rule.appliesTo(PeriodLoadRuleTest.builder.interval(new org.joda.time.Interval(now.plusDays(1), now.plusDays(2))).build(), now));
    }

    @Test
    public void testAppliesToPartialOverlap() {
        DateTime now = DateTimes.of("2012-12-31T01:00:00");
        PeriodLoadRule rule = new PeriodLoadRule(new Period("P1M"), false, ImmutableMap.of("", 0));
        Assert.assertTrue(rule.appliesTo(PeriodLoadRuleTest.builder.interval(new org.joda.time.Interval(now.minusWeeks(1), now.plusWeeks(1))).build(), now));
        Assert.assertTrue(rule.appliesTo(PeriodLoadRuleTest.builder.interval(new org.joda.time.Interval(now.minusMonths(1).minusWeeks(1), now.minusMonths(1).plusWeeks(1))).build(), now));
    }

    @Test
    public void testIncludeFuture() {
        DateTime now = DateTimes.of("2012-12-31T01:00:00");
        PeriodLoadRule includeFutureRule = new PeriodLoadRule(new Period("P2D"), true, ImmutableMap.of("", 0));
        PeriodLoadRule notIncludeFutureRule = new PeriodLoadRule(new Period("P2D"), false, ImmutableMap.of("", 0));
        Assert.assertTrue(includeFutureRule.appliesTo(PeriodLoadRuleTest.builder.interval(new org.joda.time.Interval(now.plusDays(1), now.plusDays(2))).build(), now));
        Assert.assertFalse(notIncludeFutureRule.appliesTo(PeriodLoadRuleTest.builder.interval(new org.joda.time.Interval(now.plusDays(1), now.plusDays(2))).build(), now));
    }

    /**
     * test serialize/deserilize null values of {@link PeriodLoadRule#tieredReplicants} and {@link PeriodLoadRule#includeFuture}
     */
    @Test
    public void testSerdeNull() throws Exception {
        PeriodLoadRule rule = new PeriodLoadRule(new Period("P1D"), null, null);
        ObjectMapper jsonMapper = new DefaultObjectMapper();
        Rule reread = jsonMapper.readValue(jsonMapper.writeValueAsString(rule), Rule.class);
        Assert.assertEquals(rule.getPeriod(), getPeriod());
        Assert.assertEquals(rule.isIncludeFuture(), isIncludeFuture());
        Assert.assertEquals(DEFAULT_INCLUDE_FUTURE, rule.isIncludeFuture());
        Assert.assertEquals(rule.getTieredReplicants(), getTieredReplicants());
        Assert.assertEquals(ImmutableMap.of(DEFAULT_TIER, DEFAULT_NUM_REPLICANTS), rule.getTieredReplicants());
    }

    /**
     * test mapping null values of {@link PeriodLoadRule#tieredReplicants} and {@link PeriodLoadRule#includeFuture}
     */
    @Test
    public void testMappingNull() throws Exception {
        String inputJson = "{\n" + (("      \"period\": \"P1D\",\n" + "      \"type\": \"loadByPeriod\"\n") + "    }");
        String expectedJson = ((((((((((("{\n" + ("      \"period\": \"P1D\",\n" + "      \"includeFuture\": ")) + (DEFAULT_INCLUDE_FUTURE)) + ",\n") + "      \"tieredReplicants\": {\n") + "        \"") + (DruidServer.DEFAULT_TIER)) + "\": ") + (DruidServer.DEFAULT_NUM_REPLICANTS)) + "\n") + "      },\n") + "      \"type\": \"loadByPeriod\"\n") + "    }";
        ObjectMapper jsonMapper = new DefaultObjectMapper();
        PeriodLoadRule inputPeriodLoadRule = jsonMapper.readValue(inputJson, PeriodLoadRule.class);
        PeriodLoadRule expectedPeriodLoadRule = jsonMapper.readValue(expectedJson, PeriodLoadRule.class);
        Assert.assertEquals(expectedPeriodLoadRule.getTieredReplicants(), inputPeriodLoadRule.getTieredReplicants());
        Assert.assertEquals(expectedPeriodLoadRule.getPeriod(), inputPeriodLoadRule.getPeriod());
        Assert.assertEquals(expectedPeriodLoadRule.isIncludeFuture(), inputPeriodLoadRule.isIncludeFuture());
    }
}
