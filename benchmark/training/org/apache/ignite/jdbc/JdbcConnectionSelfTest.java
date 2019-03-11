/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.jdbc;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;


/**
 * Connection test.
 */
public class JdbcConnectionSelfTest extends GridCommonAbstractTest {
    /**
     * Custom cache name.
     */
    private static final String CUSTOM_CACHE_NAME = "custom-cache";

    /**
     * Custom REST TCP port.
     */
    private static final int CUSTOM_PORT = 11212;

    /**
     * URL prefix.
     */
    private static final String URL_PREFIX = "jdbc:ignite://";

    /**
     * Host.
     */
    private static final String HOST = "127.0.0.1";

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDefaults() throws Exception {
        String url = (JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST);
        assert (DriverManager.getConnection(url)) != null;
        assert (DriverManager.getConnection((url + "/"))) != null;
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testNodeId() throws Exception {
        String url = (((JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST)) + "/?nodeId=") + (grid(0).localNode().id());
        assert (DriverManager.getConnection(url)) != null;
        url = (((((JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST)) + "/") + (JdbcConnectionSelfTest.CUSTOM_CACHE_NAME)) + "?nodeId=") + (grid(0).localNode().id());
        assert (DriverManager.getConnection(url)) != null;
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCustomCache() throws Exception {
        String url = (((JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST)) + "/") + (JdbcConnectionSelfTest.CUSTOM_CACHE_NAME);
        assert (DriverManager.getConnection(url)) != null;
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCustomPort() throws Exception {
        String url = (((JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST)) + ":") + (JdbcConnectionSelfTest.CUSTOM_PORT);
        assert (DriverManager.getConnection(url)) != null;
        assert (DriverManager.getConnection((url + "/"))) != null;
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCustomCacheNameAndPort() throws Exception {
        String url = (((((JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST)) + ":") + (JdbcConnectionSelfTest.CUSTOM_PORT)) + "/") + (JdbcConnectionSelfTest.CUSTOM_CACHE_NAME);
        assert (DriverManager.getConnection(url)) != null;
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testWrongCache() throws Exception {
        final String url = ((JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST)) + "/wrongCacheName";
        GridTestUtils.assertThrows(log, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                DriverManager.getConnection(url);
                return null;
            }
        }, SQLException.class, "Client is invalid. Probably cache name is wrong.");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testWrongPort() throws Exception {
        final String url = ((JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST)) + ":33333";
        GridTestUtils.assertThrows(log, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                DriverManager.getConnection(url);
                return null;
            }
        }, SQLException.class, "Failed to establish connection.");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testClose() throws Exception {
        String url = (JdbcConnectionSelfTest.URL_PREFIX) + (JdbcConnectionSelfTest.HOST);
        final Connection conn = DriverManager.getConnection(url);
        assert conn != null;
        assert !(conn.isClosed());
        conn.close();
        assert conn.isClosed();
        GridTestUtils.assertThrows(log, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                conn.isValid(2);
                return null;
            }
        }, SQLException.class, "Connection is closed.");
    }
}
