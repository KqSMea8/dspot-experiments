/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.confluent.ksql.internal;


import State.REBALANCING;
import State.RUNNING;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.NullPointerTester;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.metrics.Gauge;
import org.apache.kafka.common.metrics.Metrics;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class QueryStateListenerTest {
    private static final MetricName METRIC_NAME = new MetricName("bob", "g1", "d1", ImmutableMap.of());

    @Mock
    private Metrics metrics;

    @Captor
    private ArgumentCaptor<Gauge<String>> gaugeCaptor;

    private QueryStateListener listener;

    @Test
    public void shouldThrowOnNullParams() {
        new NullPointerTester().testConstructors(QueryStateListener.class, PACKAGE);
    }

    @Test
    public void shouldAddMetricOnCreation() {
        // When:
        // Listener created in setup
        // Then:
        Mockito.verify(metrics).metricName("query-status", "ksql-queries", "The current status of the given query.", ImmutableMap.of("status", "app-id"));
        Mockito.verify(metrics).addMetric(ArgumentMatchers.eq(QueryStateListenerTest.METRIC_NAME), ArgumentMatchers.isA(Gauge.class));
    }

    @Test
    public void shouldInitiallyHaveInitialState() {
        // When:
        // Listener created in setup
        // Then:
        MatcherAssert.assertThat(currentGaugeValue(), Matchers.is("-"));
    }

    @Test
    public void shouldUpdateToNewState() {
        // When:
        listener.onChange(REBALANCING, RUNNING);
        // Then:
        MatcherAssert.assertThat(currentGaugeValue(), Matchers.is("REBALANCING"));
    }

    @Test
    public void shouldRemoveMetricOnClose() {
        // When:
        listener.close();
        // Then:
        Mockito.verify(metrics).removeMetric(QueryStateListenerTest.METRIC_NAME);
    }
}
