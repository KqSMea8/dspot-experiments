/**
 * Copyright 2016 KairosDB Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kairosdb.core.telnet;


import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.net.UnknownHostException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.kairosdb.core.DataPoint;
import org.kairosdb.core.datapoints.DoubleDataPointFactoryImpl;
import org.kairosdb.core.datapoints.LongDataPoint;
import org.kairosdb.core.datapoints.LongDataPointFactoryImpl;
import org.kairosdb.core.exception.DatastoreException;
import org.kairosdb.core.exception.KairosDBException;
import org.kairosdb.eventbus.FilterEventBus;
import org.kairosdb.eventbus.Publisher;
import org.kairosdb.events.DataPointEvent;
import org.kairosdb.util.DataPointEventUtil;
import org.kairosdb.util.Tags;
import org.mockito.Mockito;


/**
 * Created with IntelliJ IDEA.
 * User: bhawkins
 * Date: 10/7/13
 * Time: 11:30 AM
 */
public class TelnetServerTest {
    private static int telnetPort;

    private static final int MAX_COMMAND_LENGTH = 1024;

    private FilterEventBus m_eventBus;

    private Publisher<DataPointEvent> m_publisher;

    private TelnetServer m_server;

    private TelnetClient m_client;

    private TestCommandProvider commandProvider;

    @Test(expected = IllegalArgumentException.class)
    public void test_constructorMaxCommandLengthLessThanOneInvalid() throws UnknownHostException {
        new TelnetServer("0:0:0:0", 80, 0, commandProvider);
    }

    @Test(expected = NullPointerException.class)
    public void test_constructorNullCommandProviderInvalid() throws UnknownHostException {
        new TelnetServer("0:0:0:0", 80, 80, null);
    }

    @Test(expected = UnknownHostException.class)
    public void test_constructorInvalidAddress() throws UnknownHostException {
        new TelnetServer("0:0:", 80, 80, commandProvider);
    }

    @Test
    public void test_emptyAddressValid() throws UnknownHostException {
        TelnetServer telnetServer = new TelnetServer("", 80, 80, commandProvider);
        MatcherAssert.assertThat(telnetServer.getAddress().getHostName(), CoreMatchers.equalTo("localhost"));
    }

    @Test
    public void test_nullAddressValid() throws UnknownHostException {
        TelnetServer telnetServer = new TelnetServer(null, 80, 80, commandProvider);
        MatcherAssert.assertThat(telnetServer.getAddress().getHostName(), CoreMatchers.equalTo("localhost"));
    }

    @Test
    public void test_extraSpaceAfterValue() throws DatastoreException {
        long now = (System.currentTimeMillis()) / 1000;
        m_client.sendText((("put test.metric " + now) + " 123  host=test_host"));
        ImmutableSortedMap<String, String> tags = Tags.create().put("host", "test_host").build();
        DataPoint dp = new LongDataPoint((now * 1000), 123);
        DataPointEventUtil.verifyEvent(m_publisher, "test.metric", tags, dp, 0);
    }

    @Test
    public void test_extraSpaceAfterMetric() throws DatastoreException {
        long now = (System.currentTimeMillis()) / 1000;
        m_client.sendText((("put test.metric  " + now) + " 123 host=test_host"));
        ImmutableSortedMap<String, String> tags = Tags.create().put("host", "test_host").build();
        DataPoint dp = new LongDataPoint((now * 1000), 123);
        DataPointEventUtil.verifyEvent(m_publisher, "test.metric", tags, dp, 0);
    }

    @Test
    public void test_extraSpaceAfterTime() throws DatastoreException {
        long now = (System.currentTimeMillis()) / 1000;
        m_client.sendText((("put test.metric " + now) + "  123 host=test_host"));
        ImmutableSortedMap<String, String> tags = Tags.create().put("host", "test_host").build();
        DataPoint dp = new LongDataPoint((now * 1000), 123);
        DataPointEventUtil.verifyEvent(m_publisher, "test.metric", tags, dp, 0);
    }

    @Test
    public void test_tripleSpaceAfterTime() throws DatastoreException {
        long now = (System.currentTimeMillis()) / 1000;
        m_client.sendText((("put test.metric " + now) + "   123 host=test_host"));
        ImmutableSortedMap<String, String> tags = Tags.create().put("host", "test_host").build();
        DataPoint dp = new LongDataPoint((now * 1000), 123);
        DataPointEventUtil.verifyEvent(m_publisher, "test.metric", tags, dp, 0);
    }

    @Test
    public void test_sendTtl() throws DatastoreException {
        long now = (System.currentTimeMillis()) / 1000;
        m_client.sendText((("put test.metric " + now) + " 123 host=test_host kairos_opt.ttl=30"));
        ImmutableSortedMap<String, String> tags = Tags.create().put("host", "test_host").build();
        DataPoint dp = new LongDataPoint((now * 1000), 123);
        DataPointEventUtil.verifyEvent(m_publisher, "test.metric", tags, dp, 30);
    }

    @Test
    public void test_MaxCommandLengthTooLong() throws DatastoreException {
        long now = (System.currentTimeMillis()) / 1000;
        String metricName = createLongString(2048);
        String tagValue = createLongString(2048);
        m_client.sendText(((((("put " + metricName) + " ") + now) + " 123 host=test_host foo=bar customer=") + tagValue));
        Mockito.verifyZeroInteractions(m_publisher);
    }

    @Test
    public void test_MaxCommandLengthSufficient() throws IOException, KairosDBException {
        TestCommandProvider commandProvider = new TestCommandProvider();
        commandProvider.putCommand("put", new PutCommand(m_eventBus, "localhost", new LongDataPointFactoryImpl(), new DoubleDataPointFactoryImpl()));
        m_server.stop();
        m_server = new TelnetServer(TelnetServerTest.telnetPort, 4148, commandProvider);
        m_server.start();
        m_client = new TelnetClient("127.0.0.1", TelnetServerTest.telnetPort);
        long now = (System.currentTimeMillis()) / 1000;
        String metricName = createLongString(2048);
        String tagValue = createLongString(2048);
        m_client.sendText(((((("put " + metricName) + " ") + now) + " 123 host=test_host foo=bar customer=") + tagValue));
        ImmutableSortedMap<String, String> tags = Tags.create().put("host", "test_host").put("foo", "bar").put("customer", tagValue).build();
        DataPoint dp = new LongDataPoint((now * 1000), 123);
        DataPointEventUtil.verifyEvent(m_publisher, metricName, tags, dp, 0);
    }
}
