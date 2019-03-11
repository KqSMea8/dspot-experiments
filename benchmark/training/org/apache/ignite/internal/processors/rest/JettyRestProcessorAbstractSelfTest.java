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
package org.apache.ignite.internal.processors.rest;


import GridRestCommand.ATOMIC_DECREMENT;
import GridRestCommand.ATOMIC_INCREMENT;
import GridRestCommand.CACHE_ADD;
import GridRestCommand.CACHE_APPEND;
import GridRestCommand.CACHE_CAS;
import GridRestCommand.CACHE_CONTAINS_KEY;
import GridRestCommand.CACHE_CONTAINS_KEYS;
import GridRestCommand.CACHE_GET;
import GridRestCommand.CACHE_GET_ALL;
import GridRestCommand.CACHE_GET_AND_PUT;
import GridRestCommand.CACHE_GET_AND_PUT_IF_ABSENT;
import GridRestCommand.CACHE_GET_AND_REMOVE;
import GridRestCommand.CACHE_GET_AND_REPLACE;
import GridRestCommand.CACHE_METADATA;
import GridRestCommand.CACHE_METRICS;
import GridRestCommand.CACHE_PREPEND;
import GridRestCommand.CACHE_PUT;
import GridRestCommand.CACHE_PUT_ALL;
import GridRestCommand.CACHE_PUT_IF_ABSENT;
import GridRestCommand.CACHE_REMOVE;
import GridRestCommand.CACHE_REMOVE_ALL;
import GridRestCommand.CACHE_REMOVE_VALUE;
import GridRestCommand.CACHE_REPLACE;
import GridRestCommand.CACHE_REPLACE_VALUE;
import GridRestCommand.CACHE_SIZE;
import GridRestCommand.CLOSE_SQL_QUERY;
import GridRestCommand.CLUSTER_ACTIVATE;
import GridRestCommand.CLUSTER_ACTIVE;
import GridRestCommand.CLUSTER_DEACTIVATE;
import GridRestCommand.CLUSTER_INACTIVE;
import GridRestCommand.DATA_REGION_METRICS;
import GridRestCommand.DATA_STORAGE_METRICS;
import GridRestCommand.EXE;
import GridRestCommand.EXECUTE_SCAN_QUERY;
import GridRestCommand.EXECUTE_SQL_FIELDS_QUERY;
import GridRestCommand.EXECUTE_SQL_QUERY;
import GridRestCommand.FETCH_SQL_QUERY;
import GridRestCommand.NAME;
import GridRestCommand.NODE;
import GridRestCommand.RESULT;
import GridRestCommand.TOPOLOGY;
import GridRestCommand.VERSION;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.IgniteVersionUtils;
import org.apache.ignite.internal.processors.cache.IgniteCacheProxy;
import org.apache.ignite.internal.processors.cache.query.GridCacheSqlMetadata;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.T2;
import org.apache.ignite.internal.util.typedef.internal.SB;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.cache.VisorCacheClearTask;
import org.apache.ignite.internal.visor.cache.VisorCacheClearTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheConfigurationCollectorTask;
import org.apache.ignite.internal.visor.cache.VisorCacheConfigurationCollectorTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheLoadTask;
import org.apache.ignite.internal.visor.cache.VisorCacheLoadTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheMetadataTask;
import org.apache.ignite.internal.visor.cache.VisorCacheMetadataTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheMetricsCollectorTask;
import org.apache.ignite.internal.visor.cache.VisorCacheMetricsCollectorTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheNodesTask;
import org.apache.ignite.internal.visor.cache.VisorCacheNodesTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCachePartitionsTask;
import org.apache.ignite.internal.visor.cache.VisorCachePartitionsTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheRebalanceTask;
import org.apache.ignite.internal.visor.cache.VisorCacheRebalanceTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheResetMetricsTask;
import org.apache.ignite.internal.visor.cache.VisorCacheResetMetricsTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheStartTask;
import org.apache.ignite.internal.visor.cache.VisorCacheStartTaskArg;
import org.apache.ignite.internal.visor.cache.VisorCacheStopTask;
import org.apache.ignite.internal.visor.cache.VisorCacheStopTaskArg;
import org.apache.ignite.internal.visor.compute.VisorComputeCancelSessionsTask;
import org.apache.ignite.internal.visor.compute.VisorComputeCancelSessionsTaskArg;
import org.apache.ignite.internal.visor.compute.VisorComputeResetMetricsTask;
import org.apache.ignite.internal.visor.compute.VisorComputeToggleMonitoringTask;
import org.apache.ignite.internal.visor.compute.VisorComputeToggleMonitoringTaskArg;
import org.apache.ignite.internal.visor.compute.VisorGatewayTask;
import org.apache.ignite.internal.visor.debug.VisorThreadDumpTask;
import org.apache.ignite.internal.visor.file.VisorFileBlockTask;
import org.apache.ignite.internal.visor.file.VisorFileBlockTaskArg;
import org.apache.ignite.internal.visor.file.VisorLatestTextFilesTask;
import org.apache.ignite.internal.visor.file.VisorLatestTextFilesTaskArg;
import org.apache.ignite.internal.visor.igfs.VisorIgfsFormatTask;
import org.apache.ignite.internal.visor.igfs.VisorIgfsFormatTaskArg;
import org.apache.ignite.internal.visor.igfs.VisorIgfsProfilerClearTask;
import org.apache.ignite.internal.visor.igfs.VisorIgfsProfilerClearTaskArg;
import org.apache.ignite.internal.visor.igfs.VisorIgfsProfilerTask;
import org.apache.ignite.internal.visor.igfs.VisorIgfsProfilerTaskArg;
import org.apache.ignite.internal.visor.igfs.VisorIgfsResetMetricsTask;
import org.apache.ignite.internal.visor.igfs.VisorIgfsResetMetricsTaskArg;
import org.apache.ignite.internal.visor.igfs.VisorIgfsSamplingStateTask;
import org.apache.ignite.internal.visor.igfs.VisorIgfsSamplingStateTaskArg;
import org.apache.ignite.internal.visor.log.VisorLogSearchTask;
import org.apache.ignite.internal.visor.log.VisorLogSearchTaskArg;
import org.apache.ignite.internal.visor.misc.VisorAckTask;
import org.apache.ignite.internal.visor.misc.VisorAckTaskArg;
import org.apache.ignite.internal.visor.misc.VisorChangeGridActiveStateTask;
import org.apache.ignite.internal.visor.misc.VisorChangeGridActiveStateTaskArg;
import org.apache.ignite.internal.visor.misc.VisorLatestVersionTask;
import org.apache.ignite.internal.visor.misc.VisorResolveHostNameTask;
import org.apache.ignite.internal.visor.node.VisorNodeConfigurationCollectorTask;
import org.apache.ignite.internal.visor.node.VisorNodeDataCollectorTask;
import org.apache.ignite.internal.visor.node.VisorNodeDataCollectorTaskArg;
import org.apache.ignite.internal.visor.node.VisorNodeEventsCollectorTask;
import org.apache.ignite.internal.visor.node.VisorNodeEventsCollectorTaskArg;
import org.apache.ignite.internal.visor.node.VisorNodeGcTask;
import org.apache.ignite.internal.visor.node.VisorNodePingTask;
import org.apache.ignite.internal.visor.node.VisorNodePingTaskArg;
import org.apache.ignite.internal.visor.node.VisorNodeSuppressedErrorsTask;
import org.apache.ignite.internal.visor.node.VisorNodeSuppressedErrorsTaskArg;
import org.apache.ignite.internal.visor.query.VisorQueryCancelTask;
import org.apache.ignite.internal.visor.query.VisorQueryCancelTaskArg;
import org.apache.ignite.internal.visor.query.VisorQueryCleanupTask;
import org.apache.ignite.internal.visor.query.VisorQueryCleanupTaskArg;
import org.apache.ignite.internal.visor.query.VisorQueryDetailMetricsCollectorTask;
import org.apache.ignite.internal.visor.query.VisorQueryDetailMetricsCollectorTaskArg;
import org.apache.ignite.internal.visor.query.VisorQueryNextPageTask;
import org.apache.ignite.internal.visor.query.VisorQueryNextPageTaskArg;
import org.apache.ignite.internal.visor.query.VisorQueryResetMetricsTask;
import org.apache.ignite.internal.visor.query.VisorQueryResetMetricsTaskArg;
import org.apache.ignite.internal.visor.query.VisorQueryTask;
import org.apache.ignite.internal.visor.query.VisorQueryTaskArg;
import org.apache.ignite.internal.visor.query.VisorRunningQueriesCollectorTask;
import org.apache.ignite.internal.visor.query.VisorRunningQueriesCollectorTaskArg;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.lang.IgniteUuid;
import org.junit.Test;


/**
 * Tests for Jetty REST protocol.
 */
@SuppressWarnings("unchecked")
public abstract class JettyRestProcessorAbstractSelfTest extends JettyRestProcessorCommonSelfTest {
    /**
     * Used to sent request charset.
     */
    private static final String CHARSET = StandardCharsets.UTF_8.name();

    /**
     *
     */
    private static boolean memoryMetricsEnabled;

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGet() throws Exception {
        jcache().put("getKey", "getVal");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "getKey");
        info(("Get command result: " + ret));
        assertCacheOperation(ret, "getVal");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGetBinaryObjects() throws Exception {
        JettyRestProcessorAbstractSelfTest.Person p = new JettyRestProcessorAbstractSelfTest.Person(1, "John", "Doe", 300);
        jcache().put(300, p);
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "keyType", "int", "key", "300");
        info(("Get command result: " + ret));
        checkJson(ret, p);
        // Test with remote node.
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "keyType", "int", "key", "300", "destId", grid(1).localNode().id().toString());
        info(("Get command result: " + ret));
        checkJson(ret, p);
        // Test with SQL.
        SqlFieldsQuery qry = new SqlFieldsQuery(("create table employee(id integer primary key, name varchar(100), salary integer);" + "insert into employee(id, name, salary) values (1, 'Alex', 300);"));
        grid(0).context().query().querySqlFields(qry, true, false);
        ret = content("SQL_PUBLIC_EMPLOYEE", CACHE_GET, "keyType", "int", "key", "1");
        info(("Get command result: " + ret));
        JsonNode res = assertResponseSucceeded(ret, false);
        assertEquals("Alex", res.get("NAME").asText());
        assertEquals(300, res.get("SALARY").asInt());
        // Test with circular reference.
        JettyRestProcessorAbstractSelfTest.CircularRef ref1 = new JettyRestProcessorAbstractSelfTest.CircularRef(1, "Alex");
        JettyRestProcessorAbstractSelfTest.CircularRef ref2 = new JettyRestProcessorAbstractSelfTest.CircularRef(2, "300");
        JettyRestProcessorAbstractSelfTest.CircularRef ref3 = new JettyRestProcessorAbstractSelfTest.CircularRef(3, "220");
        ref1.ref(ref2);
        jcache().put(220, ref1);
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "keyType", "int", "key", "220");
        info(("Get command result: " + ret));
        JsonNode json = assertResponseSucceeded(ret, false);
        assertEquals(ref1.name, json.get("name").asText());
        assertEquals(ref1.ref.toString(), json.get("ref").toString());
        ref2.ref(ref1);
        jcache().put(222, ref1);
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "keyType", "int", "key", "222");
        info(("Get command result: " + ret));
        assertResponseContainsError(ret, "Failed convert to JSON object for circular references");
        ref1.ref(ref2);
        ref2.ref(ref3);
        ref3.ref(ref1);
        jcache().put(223, ref1);
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "keyType", "int", "key", "223");
        info(("Get command result: " + ret));
        assertResponseContainsError(ret, "Failed convert to JSON object for circular references");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testNullMapKeyAndValue() throws Exception {
        Map<String, String> map1 = new HashMap<>();
        map1.put(null, null);
        map1.put("key", "value");
        jcache().put("mapKey1", map1);
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "mapKey1");
        info(("Get command result: " + ret));
        JsonNode res = validateJsonResponse(ret);
        assertEquals(F.asMap("", null, "key", "value"), JettyRestProcessorCommonSelfTest.JSON_MAPPER.treeToValue(res, HashMap.class));
        Map<String, String> map2 = new HashMap<>();
        map2.put(null, "value");
        map2.put("key", null);
        jcache().put("mapKey2", map2);
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "mapKey2");
        info(("Get command result: " + ret));
        res = validateJsonResponse(ret);
        assertEquals(F.asMap("", "value", "key", null), JettyRestProcessorCommonSelfTest.JSON_MAPPER.treeToValue(res, HashMap.class));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testSimpleObject() throws Exception {
        SimplePerson p = new SimplePerson(1, "Test", Date.valueOf("1977-01-26"), 1000.55, 39, "CIO", 25);
        jcache().put("simplePersonKey", p);
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "simplePersonKey");
        info(("Get command result: " + ret));
        JsonNode res = assertResponseSucceeded(ret, false);
        assertEquals(p.id, res.get("id").asInt());
        assertEquals(p.name, res.get("name").asText());
        assertEquals(p.birthday.toString(), res.get("birthday").asText());
        assertEquals(p.salary, res.get("salary").asDouble());
        assertNull(res.get("age"));
        assertEquals(p.post, res.get("post").asText());
        assertEquals(25, res.get("bonus").asInt());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDate() throws Exception {
        java.util.Date utilDate = new java.util.Date();
        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
        String date = formatter.format(utilDate);
        jcache().put("utilDateKey", utilDate);
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "utilDateKey");
        info(("Get command result: " + ret));
        assertCacheOperation(ret, date);
        Date sqlDate = new Date(utilDate.getTime());
        jcache().put("sqlDateKey", sqlDate);
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "sqlDateKey");
        info(("Get SQL result: " + ret));
        assertCacheOperation(ret, sqlDate.toString());
        jcache().put("timestampKey", new Timestamp(utilDate.getTime()));
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "timestampKey");
        info(("Get timestamp: " + ret));
        assertCacheOperation(ret, date);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testUUID() throws Exception {
        UUID uuid = UUID.randomUUID();
        jcache().put("uuidKey", uuid);
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "uuidKey");
        info(("Get command result: " + ret));
        assertCacheOperation(ret, uuid.toString());
        IgniteUuid igniteUuid = IgniteUuid.fromUuid(uuid);
        jcache().put("igniteUuidKey", igniteUuid);
        ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "igniteUuidKey");
        info(("Get command result: " + ret));
        assertCacheOperation(ret, igniteUuid.toString());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testTuple() throws Exception {
        T2 t = new T2("key", "value");
        jcache().put("tupleKey", t);
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET, "key", "tupleKey");
        info(("Get command result: " + ret));
        JsonNode res = assertResponseSucceeded(ret, false);
        assertEquals(t.getKey(), res.get("key").asText());
        assertEquals(t.getValue(), res.get("value").asText());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCacheSize() throws Exception {
        jcache().removeAll();
        jcache().put("getKey", "getVal");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_SIZE);
        info(("Size command result: " + ret));
        assertCacheBulkOperation(ret, 1);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIgniteName() throws Exception {
        String ret = content(null, NAME);
        info(("Name command result: " + ret));
        assertEquals(getTestIgniteInstanceName(0), validateJsonResponse(ret).asText());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGetOrCreateCache() throws Exception {
        checkGetOrCreateAndDestroy("testCache", CacheMode.PARTITIONED, 0, CacheWriteSynchronizationMode.FULL_SYNC, null, null);
        checkGetOrCreateAndDestroy("testCache", CacheMode.PARTITIONED, 3, CacheWriteSynchronizationMode.FULL_SYNC, null, null, "backups", "3");
        checkGetOrCreateAndDestroy("testCache", CacheMode.PARTITIONED, 2, CacheWriteSynchronizationMode.FULL_ASYNC, null, null, "backups", "2", "writeSynchronizationMode", CacheWriteSynchronizationMode.FULL_ASYNC.name());
        checkGetOrCreateAndDestroy("testCache", CacheMode.REPLICATED, Integer.MAX_VALUE, CacheWriteSynchronizationMode.FULL_ASYNC, null, null, "templateName", TEMPLATE_REPLICATED, "writeSynchronizationMode", CacheWriteSynchronizationMode.FULL_ASYNC.name());
        checkGetOrCreateAndDestroy("testCache", CacheMode.REPLICATED, Integer.MAX_VALUE, CacheWriteSynchronizationMode.FULL_ASYNC, null, null, "templateName", TEMPLATE_REPLICATED, "backups", "0", "writeSynchronizationMode", CacheWriteSynchronizationMode.FULL_ASYNC.name());
        checkGetOrCreateAndDestroy("testCache", CacheMode.PARTITIONED, 1, CacheWriteSynchronizationMode.FULL_ASYNC, "testGroup", null, "templateName", TEMPLATE_PARTITIONED, "backups", "1", "writeSynchronizationMode", CacheWriteSynchronizationMode.FULL_ASYNC.name(), "cacheGroup", "testGroup");
        checkGetOrCreateAndDestroy("testCache", CacheMode.PARTITIONED, 2, CacheWriteSynchronizationMode.FULL_ASYNC, null, "testDataRegion", "templateName", TEMPLATE_PARTITIONED, "backups", "2", "writeSynchronizationMode", CacheWriteSynchronizationMode.FULL_ASYNC.name(), "dataRegion", "testDataRegion");
        checkGetOrCreateAndDestroy("testCache", CacheMode.PARTITIONED, 3, CacheWriteSynchronizationMode.FULL_ASYNC, "testGroup", "testDataRegion", "templateName", TEMPLATE_PARTITIONED, "backups", "3", "writeSynchronizationMode", CacheWriteSynchronizationMode.FULL_ASYNC.name(), "cacheGroup", "testGroup", "dataRegion", "testDataRegion");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGetAll() throws Exception {
        final Map<String, String> entries = F.asMap("getKey1", "getVal1", "getKey2", "getVal2");
        jcache().putAll(entries);
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET_ALL, "k1", "getKey1", "k2", "getKey2");
        info(("Get all command result: " + ret));
        JsonNode res = assertResponseSucceeded(ret, true);
        assertTrue(res.isObject());
        assertEquals(entries, JettyRestProcessorCommonSelfTest.JSON_MAPPER.treeToValue(res, Map.class));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIncorrectPut() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, CACHE_PUT, "key", "key0");
        assertResponseContainsError(ret, "Failed to find mandatory parameter in request: val");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testContainsKey() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_CONTAINS_KEY, "key", "key0");
        assertCacheOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testContainsKeys() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        grid(0).cache(DEFAULT_CACHE_NAME).put("key1", "val1");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_CONTAINS_KEYS, "k1", "key0", "k2", "key1");
        assertCacheBulkOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGetAndPut() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET_AND_PUT, "key", "key0", "val", "val1");
        assertCacheOperation(ret, "val0");
        assertEquals("val1", grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGetAndPutIfAbsent() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET_AND_PUT_IF_ABSENT, "key", "key0", "val", "val1");
        assertCacheOperation(ret, "val0");
        assertEquals("val0", grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPutIfAbsent2() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, CACHE_PUT_IF_ABSENT, "key", "key0", "val", "val1");
        assertCacheOperation(ret, true);
        assertEquals("val1", grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testRemoveValue() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_REMOVE_VALUE, "key", "key0", "val", "val1");
        assertCacheOperation(ret, false);
        assertEquals("val0", grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
        ret = content(DEFAULT_CACHE_NAME, CACHE_REMOVE_VALUE, "key", "key0", "val", "val0");
        assertCacheOperation(ret, true);
        assertNull(grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGetAndRemove() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET_AND_REMOVE, "key", "key0");
        assertCacheOperation(ret, "val0");
        assertNull(grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReplaceValue() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_REPLACE_VALUE, "key", "key0", "val", "val1", "val2", "val2");
        assertCacheOperation(ret, false);
        assertEquals("val0", grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
        ret = content(DEFAULT_CACHE_NAME, CACHE_REPLACE_VALUE, "key", "key0", "val", "val1", "val2", "val0");
        assertCacheOperation(ret, true);
        assertEquals("val1", grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testGetAndReplace() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("key0", "val0");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_GET_AND_REPLACE, "key", "key0", "val", "val1");
        assertCacheOperation(ret, "val0");
        assertEquals("val1", grid(0).cache(DEFAULT_CACHE_NAME).get("key0"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDeactivateActivate() throws Exception {
        assertClusterState(true);
        changeClusterState(CLUSTER_DEACTIVATE);
        changeClusterState(CLUSTER_ACTIVATE);
        // same for deprecated.
        changeClusterState(CLUSTER_INACTIVE);
        changeClusterState(CLUSTER_ACTIVE);
        initCache();
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPut() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, CACHE_PUT, "key", "putKey", "val", "putVal");
        info(("Put command result: " + ret));
        assertEquals("putVal", jcache().localPeek("putKey"));
        assertCacheOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPutWithExpiration() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, CACHE_PUT, "key", "putKey", "val", "putVal", "exp", "2000");
        assertCacheOperation(ret, true);
        assertEquals("putVal", jcache().get("putKey"));
        Thread.sleep(2100);
        assertNull(jcache().get("putKey"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testAdd() throws Exception {
        jcache().put("addKey1", "addVal1");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_ADD, "key", "addKey2", "val", "addVal2");
        assertCacheOperation(ret, true);
        assertEquals("addVal1", jcache().localPeek("addKey1"));
        assertEquals("addVal2", jcache().localPeek("addKey2"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testAddWithExpiration() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, CACHE_ADD, "key", "addKey", "val", "addVal", "exp", "2000");
        assertCacheOperation(ret, true);
        assertEquals("addVal", jcache().get("addKey"));
        Thread.sleep(2100);
        assertNull(jcache().get("addKey"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPutAll() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, CACHE_PUT_ALL, "k1", "putKey1", "k2", "putKey2", "v1", "putVal1", "v2", "putVal2");
        info(("Put all command result: " + ret));
        assertEquals("putVal1", jcache().localPeek("putKey1"));
        assertEquals("putVal2", jcache().localPeek("putKey2"));
        assertCacheBulkOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testRemove() throws Exception {
        jcache().put("rmvKey", "rmvVal");
        assertEquals("rmvVal", jcache().localPeek("rmvKey"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_REMOVE, "key", "rmvKey");
        info(("Remove command result: " + ret));
        assertNull(jcache().localPeek("rmvKey"));
        assertCacheOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testRemoveAll() throws Exception {
        jcache().put("rmvKey1", "rmvVal1");
        jcache().put("rmvKey2", "rmvVal2");
        jcache().put("rmvKey3", "rmvVal3");
        jcache().put("rmvKey4", "rmvVal4");
        assertEquals("rmvVal1", jcache().localPeek("rmvKey1"));
        assertEquals("rmvVal2", jcache().localPeek("rmvKey2"));
        assertEquals("rmvVal3", jcache().localPeek("rmvKey3"));
        assertEquals("rmvVal4", jcache().localPeek("rmvKey4"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_REMOVE_ALL, "k1", "rmvKey1", "k2", "rmvKey2");
        info(("Remove all command result: " + ret));
        assertNull(jcache().localPeek("rmvKey1"));
        assertNull(jcache().localPeek("rmvKey2"));
        assertEquals("rmvVal3", jcache().localPeek("rmvKey3"));
        assertEquals("rmvVal4", jcache().localPeek("rmvKey4"));
        assertCacheBulkOperation(ret, true);
        ret = content(DEFAULT_CACHE_NAME, CACHE_REMOVE_ALL);
        info(("Remove all command result: " + ret));
        assertNull(jcache().localPeek("rmvKey1"));
        assertNull(jcache().localPeek("rmvKey2"));
        assertNull(jcache().localPeek("rmvKey3"));
        assertNull(jcache().localPeek("rmvKey4"));
        assertEquals(0, jcache().localSize());
        assertCacheBulkOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCas() throws Exception {
        jcache().put("casKey", "casOldVal");
        assertEquals("casOldVal", jcache().localPeek("casKey"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_CAS, "key", "casKey", "val2", "casOldVal", "val1", "casNewVal");
        info(("CAS command result: " + ret));
        assertEquals("casNewVal", jcache().localPeek("casKey"));
        assertCacheOperation(ret, true);
        jcache().remove("casKey");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReplace() throws Exception {
        jcache().put("repKey", "repOldVal");
        assertEquals("repOldVal", jcache().localPeek("repKey"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_REPLACE, "key", "repKey", "val", "repVal");
        info(("Replace command result: " + ret));
        assertEquals("repVal", jcache().localPeek("repKey"));
        assertCacheOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testReplaceWithExpiration() throws Exception {
        jcache().put("replaceKey", "replaceVal");
        assertEquals("replaceVal", jcache().get("replaceKey"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_REPLACE, "key", "replaceKey", "val", "replaceValNew", "exp", "2000");
        assertCacheOperation(ret, true);
        assertEquals("replaceValNew", jcache().get("replaceKey"));
        // Use larger value to avoid false positives.
        Thread.sleep(2100);
        assertNull(jcache().get("replaceKey"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testAppend() throws Exception {
        jcache().put("appendKey", "appendVal");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_APPEND, "key", "appendKey", "val", "_suffix");
        assertCacheOperation(ret, true);
        assertEquals("appendVal_suffix", jcache().get("appendKey"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPrepend() throws Exception {
        jcache().put("prependKey", "prependVal");
        String ret = content(DEFAULT_CACHE_NAME, CACHE_PREPEND, "key", "prependKey", "val", "prefix_");
        assertCacheOperation(ret, true);
        assertEquals("prefix_prependVal", jcache().get("prependKey"));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIncrement() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, ATOMIC_INCREMENT, "key", "incrKey", "init", "2", "delta", "3");
        JsonNode res = validateJsonResponse(ret);
        assertEquals(5, res.asInt());
        assertEquals(5, grid(0).atomicLong("incrKey", 0, true).get());
        ret = content(DEFAULT_CACHE_NAME, ATOMIC_INCREMENT, "key", "incrKey", "delta", "10");
        res = validateJsonResponse(ret);
        assertEquals(15, res.asInt());
        assertEquals(15, grid(0).atomicLong("incrKey", 0, true).get());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDecrement() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, ATOMIC_DECREMENT, "key", "decrKey", "init", "15", "delta", "10");
        JsonNode res = validateJsonResponse(ret);
        assertEquals(5, res.asInt());
        assertEquals(5, grid(0).atomicLong("decrKey", 0, true).get());
        ret = content(DEFAULT_CACHE_NAME, ATOMIC_DECREMENT, "key", "decrKey", "delta", "3");
        res = validateJsonResponse(ret);
        assertEquals(2, res.asInt());
        assertEquals(2, grid(0).atomicLong("decrKey", 0, true).get());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCar() throws Exception {
        jcache().put("casKey", "casOldVal");
        assertEquals("casOldVal", jcache().localPeek("casKey"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_CAS, "key", "casKey", "val2", "casOldVal");
        info(("CAR command result: " + ret));
        assertNull(jcache().localPeek("casKey"));
        assertCacheOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testPutIfAbsent() throws Exception {
        assertNull(jcache().localPeek("casKey"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_CAS, "key", "casKey", "val1", "casNewVal");
        info(("PutIfAbsent command result: " + ret));
        assertEquals("casNewVal", jcache().localPeek("casKey"));
        assertCacheOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testCasRemove() throws Exception {
        jcache().put("casKey", "casVal");
        assertEquals("casVal", jcache().localPeek("casKey"));
        String ret = content(DEFAULT_CACHE_NAME, CACHE_CAS, "key", "casKey");
        info(("CAS Remove command result: " + ret));
        assertNull(jcache().localPeek("casKey"));
        assertCacheOperation(ret, true);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testMetrics() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, CACHE_METRICS);
        info(("Cache metrics command result: " + ret));
        assertCacheMetrics(ret);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testMetadataLocal() throws Exception {
        IgniteCacheProxy<?, ?> cache = F.first(grid(0).context().cache().publicCaches());
        assertNotNull("Should have configured public cache!", cache);
        Collection<GridCacheSqlMetadata> metas = cache.context().queries().sqlMetadata();
        // TODO: IGNITE-7740 uncomment after IGNITE-7740 will be fixed.
        // int cachesCnt = grid(0).cacheNames().size();
        // assertEquals(cachesCnt, metas.size());
        String ret = content("", CACHE_METADATA);
        info(("Cache metadata: " + ret));
        JsonNode arrRes = validateJsonResponse(ret);
        // TODO: IGNITE-7740 uncomment after IGNITE-7740 will be fixed.
        // assertEquals(cachesCnt, arrRes.size());
        testMetadata(metas, arrRes);
        Collection<GridCacheSqlMetadata> dfltCacheMeta = cache.context().queries().sqlMetadata();
        ret = content(DEFAULT_CACHE_NAME, CACHE_METADATA);
        info(("Cache metadata: " + ret));
        arrRes = validateJsonResponse(ret);
        assertEquals(1, arrRes.size());
        testMetadata(dfltCacheMeta, arrRes);
        assertResponseContainsError(content("nonExistingCacheName", CACHE_METADATA), "Failed to request meta data. nonExistingCacheName is not found");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testMetadataRemote() throws Exception {
        CacheConfiguration<Integer, String> partialCacheCfg = new CacheConfiguration("partial");
        partialCacheCfg.setIndexedTypes(Integer.class, String.class);
        partialCacheCfg.setNodeFilter(new JettyRestProcessorAbstractSelfTest.NodeIdFilter(grid(1).localNode().id()));
        IgniteCacheProxy<Integer, String> c = ((IgniteCacheProxy<Integer, String>) (grid(1).createCache(partialCacheCfg)));
        Collection<GridCacheSqlMetadata> metas = c.context().queries().sqlMetadata();
        String ret = content("", CACHE_METADATA);
        info(("Cache metadata: " + ret));
        JsonNode arrRes = validateJsonResponse(ret);
        // TODO: IGNITE-7740 uncomment after IGNITE-7740 will be fixed.
        // int cachesCnt = grid(1).cacheNames().size();
        // assertEquals(cachesCnt, arrRes.size());
        testMetadata(metas, arrRes);
        ret = content("person", CACHE_METADATA);
        info(("Cache metadata with cacheName parameter: " + ret));
        arrRes = validateJsonResponse(ret);
        assertEquals(1, arrRes.size());
        testMetadata(metas, arrRes);
        assertResponseContainsError(content("nonExistingCacheName", CACHE_METADATA), "Failed to request meta data. nonExistingCacheName is not found");
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testTopology() throws Exception {
        String ret = content(null, TOPOLOGY, "attr", "false", "mtr", "false");
        info(("Topology command result: " + ret));
        JsonNode res = validateJsonResponse(ret);
        assertEquals(gridCount(), res.size());
        for (JsonNode node : res) {
            assertTrue(node.get("attributes").isNull());
            assertTrue(node.get("metrics").isNull());
            JsonNode caches = node.get("caches");
            assertFalse(caches.isNull());
            Collection<IgniteCacheProxy<?, ?>> publicCaches = grid(0).context().cache().publicCaches();
            assertEquals(publicCaches.size(), caches.size());
            for (JsonNode cache : caches) {
                String cacheName0 = cache.get("name").asText();
                final String cacheName = (cacheName0.isEmpty()) ? null : cacheName0;
                IgniteCacheProxy<?, ?> publicCache = F.find(publicCaches, null, new org.apache.ignite.internal.util.typedef.P1<IgniteCacheProxy<?, ?>>() {
                    @Override
                    public boolean apply(IgniteCacheProxy<?, ?> c) {
                        return F.eq(c.getName(), cacheName);
                    }
                });
                assertNotNull(publicCache);
                CacheMode cacheMode = CacheMode.valueOf(cache.get("mode").asText());
                assertEquals(publicCache.getConfiguration(CacheConfiguration.class).getCacheMode(), cacheMode);
            }
        }
        // Test that caches not included.
        ret = content(null, TOPOLOGY, "attr", "false", "mtr", "false", "caches", "false");
        info(("Topology command result: " + ret));
        res = validateJsonResponse(ret);
        assertEquals(gridCount(), res.size());
        for (JsonNode node : res) {
            assertTrue(node.get("attributes").isNull());
            assertTrue(node.get("metrics").isNull());
            assertTrue(node.get("caches").isNull());
        }
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testNode() throws Exception {
        String ret = content(null, NODE, "attr", "true", "mtr", "true", "id", grid(0).localNode().id().toString());
        info(("Topology command result: " + ret));
        JsonNode res = validateJsonResponse(ret);
        assertTrue(res.get("attributes").isObject());
        assertTrue(res.get("metrics").isObject());
        JsonNode caches = res.get("caches");
        assertTrue(caches.isArray());
        assertFalse(caches.isNull());
        assertEquals(grid(0).context().cache().publicCaches().size(), caches.size());
        ret = content(null, NODE, "attr", "false", "mtr", "false", "ip", AbstractRestProcessorSelfTest.LOC_HOST);
        info(("Topology command result: " + ret));
        res = validateJsonResponse(ret);
        assertTrue(res.get("attributes").isNull());
        assertTrue(res.get("metrics").isNull());
        ret = content(null, NODE, "attr", "false", "mtr", "false", "ip", AbstractRestProcessorSelfTest.LOC_HOST, "id", UUID.randomUUID().toString());
        info(("Topology command result: " + ret));
        res = validateJsonResponse(ret);
        assertTrue(res.isNull());
        // Check that caches not included.
        ret = content(null, NODE, "id", grid(0).localNode().id().toString(), "attr", "false", "mtr", "false", "caches", "false");
        info(("Topology command result: " + ret));
        res = validateJsonResponse(ret);
        assertTrue(res.get("attributes").isNull());
        assertTrue(res.get("metrics").isNull());
        assertTrue(res.get("caches").isNull());
    }

    /**
     * Tests {@code exe} command.
     * <p>
     * Note that attempt to execute unknown task (UNKNOWN_TASK) will result in exception on server.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testExe() throws Exception {
        String ret = content(DEFAULT_CACHE_NAME, EXE);
        info(("Exe command result: " + ret));
        assertResponseContainsError(ret, "Failed to find mandatory parameter in request: name");
        // Attempt to execute unknown task (UNKNOWN_TASK) will result in exception on server.
        ret = content(DEFAULT_CACHE_NAME, EXE, "name", "UNKNOWN_TASK");
        info(("Exe command result: " + ret));
        assertResponseContainsError(ret, "Unknown task name or failed to auto-deploy task (was task (re|un)deployed?)");
        grid(0).compute().localDeployTask(TestTask1.class, TestTask1.class.getClassLoader());
        grid(0).compute().localDeployTask(TestTask2.class, TestTask2.class.getClassLoader());
        ret = content(DEFAULT_CACHE_NAME, EXE, "name", TestTask1.class.getName());
        info(("Exe command result: " + ret));
        JsonNode res = jsonTaskResult(ret);
        assertTrue(res.isNull());
        ret = content(DEFAULT_CACHE_NAME, EXE, "name", TestTask2.class.getName());
        info(("Exe command result: " + ret));
        res = jsonTaskResult(ret);
        assertEquals(TestTask2.RES, res.asText());
        ret = content(DEFAULT_CACHE_NAME, RESULT);
        info(("Exe command result: " + ret));
        assertResponseContainsError(ret, "Failed to find mandatory parameter in request: id");
    }

    /**
     * Tests execution of Visor tasks via {@link VisorGatewayTask}.
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testVisorGateway() throws Exception {
        ClusterNode locNode = grid(1).localNode();
        final IgniteUuid cid = grid(1).context().cache().internalCache("person").context().dynamicDeploymentId();
        String ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheConfigurationCollectorTask.class).forNode(locNode).argument(VisorCacheConfigurationCollectorTaskArg.class).collection(IgniteUuid.class, cid));
        info(("VisorCacheConfigurationCollectorTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheNodesTask.class).forNode(locNode).argument(VisorCacheNodesTaskArg.class, "person"));
        info(("VisorCacheNodesTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCachePartitionsTask.class).forNode(locNode).argument(VisorCachePartitionsTaskArg.class, "person"));
        info(("VisorCachePartitionsTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheLoadTask.class).forNode(locNode).argument(VisorCacheLoadTaskArg.class).set(String.class, "person").arguments(0, "null"));
        info(("VisorCacheLoadTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheRebalanceTask.class).forNode(locNode).argument(VisorCacheRebalanceTaskArg.class).set(String.class, "person"));
        info(("VisorCacheRebalanceTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheMetadataTask.class).forNode(locNode).argument(VisorCacheMetadataTaskArg.class, "person"));
        info(("VisorCacheMetadataTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheResetMetricsTask.class).forNode(locNode).argument(VisorCacheResetMetricsTaskArg.class, "person"));
        info(("VisorCacheResetMetricsTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorIgfsSamplingStateTask.class).forNode(locNode).argument(VisorIgfsSamplingStateTaskArg.class, "igfs", false));
        info(("VisorIgfsSamplingStateTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorIgfsProfilerClearTask.class).forNode(locNode).argument(VisorIgfsProfilerClearTaskArg.class, "igfs"));
        info(("VisorIgfsProfilerClearTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorIgfsProfilerTask.class).forNode(locNode).argument(VisorIgfsProfilerTaskArg.class, "igfs"));
        info(("VisorIgfsProfilerTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorIgfsFormatTask.class).forNode(locNode).argument(VisorIgfsFormatTaskArg.class, "igfs"));
        info(("VisorIgfsFormatTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorIgfsResetMetricsTask.class).forNode(locNode).argument(VisorIgfsResetMetricsTaskArg.class).set(String.class, "igfs"));
        info(("VisorIgfsResetMetricsTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorThreadDumpTask.class).forNode(locNode));
        info(("VisorThreadDumpTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorLatestTextFilesTask.class).forNode(locNode).argument(VisorLatestTextFilesTaskArg.class, "", ""));
        info(("VisorLatestTextFilesTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorLatestVersionTask.class).forNode(locNode));
        info(("VisorLatestVersionTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorFileBlockTask.class).forNode(locNode).argument(VisorFileBlockTaskArg.class, "", 0L, 1, 0L));
        info(("VisorFileBlockTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorNodePingTask.class).forNode(locNode).argument(VisorNodePingTaskArg.class, locNode.id()));
        info(("VisorNodePingTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorNodeConfigurationCollectorTask.class).forNode(locNode));
        info(("VisorNodeConfigurationCollectorTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorComputeResetMetricsTask.class).forNode(locNode));
        info(("VisorComputeResetMetricsTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryTask.class).forNode(locNode).argument(VisorQueryTaskArg.class, "person", URLEncoder.encode("select * from Person", JettyRestProcessorAbstractSelfTest.CHARSET), false, false, false, false, 1));
        info(("VisorQueryTask result: " + ret));
        JsonNode res = jsonTaskResult(ret);
        final String qryId = res.get("result").get("queryId").asText();
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryNextPageTask.class).forNode(locNode).argument(VisorQueryNextPageTaskArg.class, qryId, 1));
        info(("VisorQueryNextPageTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryCleanupTask.class).argument(VisorQueryCleanupTaskArg.class).map(UUID.class, Set.class, F.asMap(locNode.id(), qryId)));
        info(("VisorQueryCleanupTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorResolveHostNameTask.class).forNode(locNode));
        info(("VisorResolveHostNameTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryCancelTask.class).argument(VisorQueryCancelTaskArg.class, 0L));
        info(("VisorResolveHostNameTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryResetMetricsTask.class).argument(VisorQueryResetMetricsTaskArg.class, "person"));
        info(("VisorResolveHostNameTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryCancelTask.class).argument(VisorQueryCancelTaskArg.class, 0L));
        info(("VisorResolveHostNameTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryResetMetricsTask.class).argument(VisorQueryResetMetricsTaskArg.class, "person"));
        info(("VisorResolveHostNameTask result: " + ret));
        jsonTaskResult(ret);
        // Multinode tasks
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorComputeCancelSessionsTask.class).argument(VisorComputeCancelSessionsTaskArg.class).set(IgniteUuid.class, IgniteUuid.randomUuid()));
        info(("VisorComputeCancelSessionsTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheMetricsCollectorTask.class).argument(VisorCacheMetricsCollectorTaskArg.class, false).collection(String.class, "person"));
        info(("VisorCacheMetricsCollectorTask result: " + ret));
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheMetricsCollectorTask.class).forNodes(grid(1).cluster().nodes()).argument(VisorCacheMetricsCollectorTaskArg.class, false).collection(String.class, "person"));
        info(("VisorCacheMetricsCollectorTask (with nodes) result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorLogSearchTask.class).argument(VisorLogSearchTaskArg.class, ".", ".", "abrakodabra.txt", 1));
        info(("VisorLogSearchTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorNodeGcTask.class));
        info(("VisorNodeGcTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorAckTask.class).argument(VisorAckTaskArg.class, "MSG"));
        info(("VisorAckTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorNodeEventsCollectorTask.class).argument(VisorNodeEventsCollectorTaskArg.class, "null", "null", "null", "taskName", "null"));
        info(("VisorNodeEventsCollectorTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorNodeDataCollectorTask.class).argument(VisorNodeDataCollectorTaskArg.class, false, ("CONSOLE_" + (UUID.randomUUID())), UUID.randomUUID(), false));
        info(("VisorNodeDataCollectorTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorComputeToggleMonitoringTask.class).argument(VisorComputeToggleMonitoringTaskArg.class, UUID.randomUUID(), false));
        info(("VisorComputeToggleMonitoringTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorNodeSuppressedErrorsTask.class).argument(VisorNodeSuppressedErrorsTaskArg.class).map(UUID.class, Long.class, new HashMap()));
        info(("VisorNodeSuppressedErrorsTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheClearTask.class).forNode(locNode).argument(VisorCacheClearTaskArg.class, "person"));
        info(("VisorCacheClearTask result: " + ret));
        jsonTaskResult(ret);
        // Spring XML to start cache via Visor task.
        final String START_CACHE = "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + ((((((("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + "    xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n") + "        http://www.springframework.org/schema/beans/spring-beans-2.5.xsd\">\n") + "    <bean id=\"cacheConfiguration\" class=\"org.apache.ignite.configuration.CacheConfiguration\">\n") + "        <property name=\"cacheMode\" value=\"PARTITIONED\"/>\n") + "        <property name=\"name\" value=\"c\"/>\n") + "   </bean>\n") + "</beans>");
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheStartTask.class).argument(VisorCacheStartTaskArg.class, false, "person2", URLEncoder.encode(START_CACHE, JettyRestProcessorAbstractSelfTest.CHARSET)));
        info(("VisorCacheStartTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorCacheStopTask.class).forNode(locNode).argument(VisorCacheStopTaskArg.class, "c"));
        info(("VisorCacheStopTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorQueryDetailMetricsCollectorTask.class).argument(VisorQueryDetailMetricsCollectorTaskArg.class, 0));
        info(("VisorQueryDetailMetricsCollectorTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorRunningQueriesCollectorTask.class).argument(VisorRunningQueriesCollectorTaskArg.class, 0L));
        info(("VisorQueryDetailMetricsCollectorTask result: " + ret));
        jsonTaskResult(ret);
        ret = content(new JettyRestProcessorAbstractSelfTest.VisorGatewayArgument(VisorChangeGridActiveStateTask.class).argument(VisorChangeGridActiveStateTaskArg.class, true));
        info(("VisorQueryDetailMetricsCollectorTask result: " + ret));
        jsonTaskResult(ret);
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDataRegionMetrics() throws Exception {
        String ret = content(F.asMap("cmd", DATA_REGION_METRICS.key()));
        JsonNode res = validateJsonResponse(ret);
        assertTrue(((res.size()) > 0));
        info((((DATA_REGION_METRICS.key().toUpperCase()) + " command result: ") + ret));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDataStorageMetricsDisabled() throws Exception {
        String ret = content(F.asMap("cmd", DATA_STORAGE_METRICS.key()));
        JsonNode res = validateJsonResponse(ret);
        assertTrue(res.asText().equalsIgnoreCase("Storage metrics are not enabled"));
        info((((DATA_STORAGE_METRICS.key().toUpperCase()) + " command result: ") + ret));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDataStorageMetricsEnabled() throws Exception {
        if (!(JettyRestProcessorAbstractSelfTest.memoryMetricsEnabled)) {
            restartGrid();
            JettyRestProcessorAbstractSelfTest.memoryMetricsEnabled = true;
        }
        String ret = content(F.asMap("cmd", DATA_STORAGE_METRICS.key()));
        assertNotNull(validateJsonResponse(ret));
        info((((DATA_STORAGE_METRICS.key().toUpperCase()) + " command result: ") + ret));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testVersion() throws Exception {
        String ret = content(null, VERSION);
        JsonNode res = validateJsonResponse(ret);
        assertEquals(IgniteVersionUtils.VER_STR, res.asText());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testQueryArgs() throws Exception {
        String qry = "salary > ? and salary <= ?";
        String ret = content("person", EXECUTE_SQL_QUERY, "type", "Person", "pageSize", "10", "qry", URLEncoder.encode(qry, JettyRestProcessorAbstractSelfTest.CHARSET), "arg1", "1000", "arg2", "2000");
        JsonNode items = validateJsonResponse(ret).get("items");
        assertEquals(2, items.size());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testQueryScan() throws Exception {
        String ret = content("person", EXECUTE_SCAN_QUERY, "pageSize", "10", "cacheName", "person");
        JsonNode items = validateJsonResponse(ret).get("items");
        assertEquals(4, items.size());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testFilterQueryScan() throws Exception {
        String ret = content("person", EXECUTE_SCAN_QUERY, "pageSize", "10", "className", JettyRestProcessorAbstractSelfTest.ScanFilter.class.getName());
        JsonNode items = validateJsonResponse(ret).get("items");
        assertEquals(2, items.size());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testIncorrectFilterQueryScan() throws Exception {
        String clsName = (JettyRestProcessorAbstractSelfTest.ScanFilter.class.getName()) + 1;
        String ret = content("person", EXECUTE_SCAN_QUERY, "pageSize", "10", "className", clsName);
        assertResponseContainsError(ret, ("Failed to find target class: " + clsName));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testQuery() throws Exception {
        grid(0).cache(DEFAULT_CACHE_NAME).put("1", "1");
        grid(0).cache(DEFAULT_CACHE_NAME).put("2", "2");
        grid(0).cache(DEFAULT_CACHE_NAME).put("3", "3");
        String ret = content(DEFAULT_CACHE_NAME, EXECUTE_SQL_QUERY, "type", "String", "pageSize", "1", "qry", URLEncoder.encode("select * from String", JettyRestProcessorAbstractSelfTest.CHARSET));
        JsonNode qryId = validateJsonResponse(ret).get("queryId");
        assertFalse(validateJsonResponse(ret).get("queryId").isNull());
        ret = content(DEFAULT_CACHE_NAME, FETCH_SQL_QUERY, "pageSize", "1", "qryId", qryId.asText());
        JsonNode res = validateJsonResponse(ret);
        JsonNode qryId0 = validateJsonResponse(ret).get("queryId");
        assertEquals(qryId0, qryId);
        assertFalse(res.get("last").asBoolean());
        ret = content(DEFAULT_CACHE_NAME, FETCH_SQL_QUERY, "pageSize", "1", "qryId", qryId.asText());
        res = validateJsonResponse(ret);
        qryId0 = validateJsonResponse(ret).get("queryId");
        assertEquals(qryId0, qryId);
        assertTrue(res.get("last").asBoolean());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDistributedJoinsQuery() throws Exception {
        String qry = "select * from Person, \"organization\".Organization " + ("where \"organization\".Organization.id = Person.orgId " + "and \"organization\".Organization.name = ?");
        String ret = content("person", EXECUTE_SQL_QUERY, "type", "Person", "distributedJoins", "true", "pageSize", "10", "qry", URLEncoder.encode(qry, JettyRestProcessorAbstractSelfTest.CHARSET), "arg1", "o1");
        JsonNode items = validateJsonResponse(ret).get("items");
        assertEquals(2, items.size());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testSqlFieldsQuery() throws Exception {
        String qry = "select concat(firstName, ' ', lastName) from Person";
        String ret = content("person", EXECUTE_SQL_FIELDS_QUERY, "pageSize", "10", "qry", URLEncoder.encode(qry, JettyRestProcessorAbstractSelfTest.CHARSET));
        JsonNode items = validateJsonResponse(ret).get("items");
        assertEquals(4, items.size());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testDistributedJoinsSqlFieldsQuery() throws Exception {
        String qry = "select * from \"person\".Person p, \"organization\".Organization o where o.id = p.orgId";
        String ret = content("person", EXECUTE_SQL_FIELDS_QUERY, "distributedJoins", "true", "pageSize", "10", "qry", URLEncoder.encode(qry, JettyRestProcessorAbstractSelfTest.CHARSET));
        JsonNode items = validateJsonResponse(ret).get("items");
        assertEquals(4, items.size());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testSqlFieldsMetadataQuery() throws Exception {
        String qry = "select firstName, lastName from Person";
        String ret = content("person", EXECUTE_SQL_FIELDS_QUERY, "pageSize", "10", "qry", URLEncoder.encode(qry, JettyRestProcessorAbstractSelfTest.CHARSET));
        JsonNode res = validateJsonResponse(ret);
        JsonNode items = res.get("items");
        JsonNode meta = res.get("fieldsMetadata");
        assertEquals(4, items.size());
        assertEquals(2, meta.size());
        JsonNode o = meta.get(0);
        assertEquals("FIRSTNAME", o.get("fieldName").asText());
        assertEquals("java.lang.String", o.get("fieldTypeName").asText());
        assertEquals("person", o.get("schemaName").asText());
        assertEquals("PERSON", o.get("typeName").asText());
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testQueryClose() throws Exception {
        String qry = "salary > ? and salary <= ?";
        String ret = content("person", EXECUTE_SQL_QUERY, "type", "Person", "pageSize", "1", "qry", URLEncoder.encode(qry, JettyRestProcessorAbstractSelfTest.CHARSET), "arg1", "1000", "arg2", "2000");
        JsonNode res = validateJsonResponse(ret);
        assertEquals(1, res.get("items").size());
        assertTrue(queryCursorFound());
        assertFalse(res.get("queryId").isNull());
        String qryId = res.get("queryId").asText();
        content("person", CLOSE_SQL_QUERY, "qryId", qryId);
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testQueryDelay() throws Exception {
        String qry = "salary > ? and salary <= ?";
        String ret = null;
        for (int i = 0; i < 10; ++i)
            ret = content("person", EXECUTE_SQL_QUERY, "type", "Person", "pageSize", "1", "qry", URLEncoder.encode(qry, JettyRestProcessorAbstractSelfTest.CHARSET), "arg1", "1000", "arg2", "2000");

        JsonNode items = validateJsonResponse(ret).get("items");
        assertEquals(1, items.size());
        assertTrue(queryCursorFound());
        U.sleep(10000);
        assertFalse(queryCursorFound());
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testTypedPut() throws Exception {
        // Test boolean type.
        putTypedValue("boolean", "true", "false", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.lang.Boolean", "false", "true", GridRestResponse.STATUS_SUCCESS);
        IgniteCache<Boolean, Boolean> cBool = typedCache();
        assertEquals(Boolean.FALSE, cBool.get(true));
        assertEquals(Boolean.TRUE, cBool.get(false));
        // Test byte type.
        putTypedValue("byte", "64", "100", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.lang.Byte", "-25", "-127", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("byte", "65", "aaa", GridRestResponse.STATUS_FAILED);
        putTypedValue("byte", "aaa", "64", GridRestResponse.STATUS_FAILED);
        putTypedValue("byte", "aaa", "aaa", GridRestResponse.STATUS_FAILED);
        IgniteCache<Byte, Byte> cByte = typedCache();
        assertEquals(Byte.valueOf("100"), cByte.get(Byte.valueOf("64")));
        assertEquals(Byte.valueOf("-127"), cByte.get(Byte.valueOf("-25")));
        // Test short type.
        putTypedValue("short", "1024", "4096", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.lang.Short", "-15000", "-16000", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("short", "1025", "bbb", GridRestResponse.STATUS_FAILED);
        putTypedValue("short", "bbb", "5", GridRestResponse.STATUS_FAILED);
        putTypedValue("short", "bbb", "bbb", GridRestResponse.STATUS_FAILED);
        IgniteCache<Short, Short> cShort = typedCache();
        assertEquals(Short.valueOf("4096"), cShort.get(Short.valueOf("1024")));
        assertEquals(Short.valueOf("-16000"), cShort.get(Short.valueOf("-15000")));
        // Test integer type.
        putTypedValue("int", "65555", "128256", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("Integer", "74555", "200000", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.lang.Integer", "-200", "-100000", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("int", "0", "ccc", GridRestResponse.STATUS_FAILED);
        putTypedValue("int", "ccc", "0", GridRestResponse.STATUS_FAILED);
        putTypedValue("int", "ccc", "ccc", GridRestResponse.STATUS_FAILED);
        IgniteCache<Integer, Integer> cInt = typedCache();
        assertEquals(Integer.valueOf(128256), cInt.get(65555));
        assertEquals(Integer.valueOf(200000), cInt.get(74555));
        assertEquals(Integer.valueOf((-100000)), cInt.get((-200)));
        // Test long type.
        putTypedValue("long", "3000000", "400000", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.lang.Long", "-3000000", "-400000", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("long", "777", "ddd", GridRestResponse.STATUS_FAILED);
        putTypedValue("long", "ddd", "777", GridRestResponse.STATUS_FAILED);
        putTypedValue("long", "ddd", "ddd", GridRestResponse.STATUS_FAILED);
        IgniteCache<Long, Long> cLong = typedCache();
        assertEquals(Long.valueOf(400000), cLong.get(3000000L));
        assertEquals(Long.valueOf((-400000)), cLong.get((-3000000L)));
        // Test float type.
        putTypedValue("float", "1.5", "2.5", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.lang.Float", "-7.5", "-8.5", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("float", "1.5", "hhh", GridRestResponse.STATUS_FAILED);
        putTypedValue("float", "hhh", "1.5", GridRestResponse.STATUS_FAILED);
        putTypedValue("float", "hhh", "hhh", GridRestResponse.STATUS_FAILED);
        IgniteCache<Float, Float> cFloat = typedCache();
        assertEquals(2.5F, cFloat.get(1.5F));
        assertEquals((-8.5F), cFloat.get((-7.5F)));
        // Test double type.
        putTypedValue("double", "5.5", "75.5", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.lang.Double", "-155.5", "-255.5", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("double", "jjj", "75.5", GridRestResponse.STATUS_FAILED);
        putTypedValue("double", "6.5", "jjj", GridRestResponse.STATUS_FAILED);
        putTypedValue("double", "jjj", "jjj", GridRestResponse.STATUS_FAILED);
        IgniteCache<Double, Double> cDouble = typedCache();
        assertEquals(75.5, cDouble.get(5.5));
        assertEquals((-255.5), cDouble.get((-155.5)));
        // Test date type.
        putTypedValue("date", "2018-02-18", "2017-01-01", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.sql.Date", "2018-01-01", "2017-02-02", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("date", "xxxx-yy-mm", "2017-01-01", GridRestResponse.STATUS_FAILED);
        putTypedValue("date", "2018-03-18", "xxxx-yy-mm", GridRestResponse.STATUS_FAILED);
        putTypedValue("date", "xxxx-yy-mm", "xxxx-yy-mm", GridRestResponse.STATUS_FAILED);
        IgniteCache<Date, Date> cDate = typedCache();
        assertEquals(Date.valueOf("2017-01-01"), cDate.get(Date.valueOf("2018-02-18")));
        assertEquals(Date.valueOf("2017-02-02"), cDate.get(Date.valueOf("2018-01-01")));
        // Test time type.
        putTypedValue("Time", "01:01:01", "02:02:02", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.sql.Time", "03:03:03", "04:04:04", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("Time", "aa:bb:dd", "02:02:02", GridRestResponse.STATUS_FAILED);
        putTypedValue("Time", "01:01:01", "zz:vv:pp", GridRestResponse.STATUS_FAILED);
        putTypedValue("Time", "zz:zz:zz", "zz:zz:zz", GridRestResponse.STATUS_FAILED);
        IgniteCache<Time, Time> cTime = typedCache();
        assertEquals(Time.valueOf("02:02:02"), cTime.get(Time.valueOf("01:01:01")));
        assertEquals(Time.valueOf("04:04:04"), cTime.get(Time.valueOf("03:03:03")));
        // Test timestamp type.
        putTypedValue("Timestamp", "2018-02-18%2001:01:01", "2017-01-01%2002:02:02", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("java.sql.timestamp", "2018-01-01%2001:01:01", "2018-05-05%2005:05:05", GridRestResponse.STATUS_SUCCESS);
        putTypedValue("timestamp", "error", "2018-03-18%2001:01:01", GridRestResponse.STATUS_FAILED);
        putTypedValue("timestamp", "2018-03-18%2001:01:01", "error", GridRestResponse.STATUS_FAILED);
        putTypedValue("timestamp", "error", "error", GridRestResponse.STATUS_FAILED);
        IgniteCache<Timestamp, Timestamp> cTs = typedCache();
        assertEquals(Timestamp.valueOf("2017-01-01 02:02:02"), cTs.get(Timestamp.valueOf("2018-02-18 01:01:01")));
        assertEquals(Timestamp.valueOf("2018-05-05 05:05:05"), cTs.get(Timestamp.valueOf("2018-01-01 01:01:01")));
        // Test UUID type.
        UUID k1 = UUID.fromString("121f5ae8-148d-11e8-b642-0ed5f89f718b");
        UUID v1 = UUID.fromString("64c6c225-b31c-4000-b136-ef14562ac785");
        putTypedValue("UUID", k1.toString(), v1.toString(), GridRestResponse.STATUS_SUCCESS);
        putTypedValue("UUID", "error", v1.toString(), GridRestResponse.STATUS_FAILED);
        putTypedValue("UUID", k1.toString(), "error", GridRestResponse.STATUS_FAILED);
        putTypedValue("UUID", "error", "error", GridRestResponse.STATUS_FAILED);
        UUID k2 = UUID.randomUUID();
        UUID v2 = UUID.randomUUID();
        putTypedValue("java.util.UUID", k2.toString(), v2.toString(), GridRestResponse.STATUS_SUCCESS);
        IgniteCache<UUID, UUID> cUUID = typedCache();
        assertEquals(v1, cUUID.get(k1));
        assertEquals(v2, cUUID.get(k2));
        // Test IgniteUuid type.
        IgniteUuid ik1 = IgniteUuid.randomUuid();
        IgniteUuid iv1 = IgniteUuid.randomUuid();
        putTypedValue("IgniteUuid", ik1.toString(), iv1.toString(), GridRestResponse.STATUS_SUCCESS);
        putTypedValue("IgniteUuid", "error", iv1.toString(), GridRestResponse.STATUS_FAILED);
        putTypedValue("IgniteUuid", ik1.toString(), "error", GridRestResponse.STATUS_FAILED);
        putTypedValue("IgniteUuid", "error", "error", GridRestResponse.STATUS_FAILED);
        IgniteUuid ik2 = IgniteUuid.randomUuid();
        IgniteUuid iv2 = IgniteUuid.randomUuid();
        putTypedValue("org.apache.ignite.lang.IgniteUuid", ik2.toString(), iv2.toString(), GridRestResponse.STATUS_SUCCESS);
        IgniteCache<IgniteUuid, IgniteUuid> cIgniteUUID = typedCache();
        assertEquals(iv1, cIgniteUUID.get(ik1));
        assertEquals(iv2, cIgniteUUID.get(ik2));
    }

    /**
     *
     *
     * @throws Exception
     * 		If failed.
     */
    @Test
    public void testTypedGet() throws Exception {
        // Test boolean type.
        IgniteCache<Boolean, Boolean> cBool = typedCache();
        cBool.put(true, false);
        cBool.put(false, true);
        getTypedValue("boolean", "true", "false");
        getTypedValue("java.lang.Boolean", "false", "true");
        // Test byte type.
        IgniteCache<Byte, Byte> cByte = typedCache();
        cByte.put(((byte) (77)), ((byte) (55)));
        cByte.put(((byte) (-88)), ((byte) (-10)));
        getTypedValue("byte", "77", "55");
        getTypedValue("java.lang.Byte", "-88", "-10");
        // Test short type.
        IgniteCache<Short, Short> cShort = typedCache();
        cShort.put(((short) (2222)), ((short) (3333)));
        cShort.put(((short) (-11111)), ((short) (-12222)));
        getTypedValue("short", "2222", "3333");
        getTypedValue("java.lang.Short", "-11111", "-12222");
        // Test integer type.
        IgniteCache<Integer, Integer> cInt = typedCache();
        cInt.put(65555, 128256);
        cInt.put(74555, 200000);
        cInt.put((-200), (-100000));
        getTypedValue("int", "65555", "128256");
        getTypedValue("Integer", "74555", "200000");
        getTypedValue("java.lang.Integer", "-200", "-100000");
        // Test long type.
        IgniteCache<Long, Long> cLong = typedCache();
        cLong.put(3333333L, 4444444L);
        cLong.put((-3333333L), (-4444444L));
        getTypedValue("long", "3333333", "4444444");
        getTypedValue("java.lang.Long", "-3333333", "-4444444");
        // Test float type.
        IgniteCache<Float, Float> cFloat = typedCache();
        cFloat.put(11.5F, 21.5F);
        cFloat.put((-71.5F), (-81.5F));
        getTypedValue("float", "11.5", "21.5");
        getTypedValue("java.lang.Float", "-71.5", "-81.5");
        // Test double type.
        IgniteCache<Double, Double> cDouble = typedCache();
        cDouble.put(58.5, 758.5);
        cDouble.put((-1558.5), (-2558.5));
        getTypedValue("double", "58.5", "758.5");
        getTypedValue("java.lang.Double", "-1558.5", "-2558.5");
        // Test date type.
        IgniteCache<Date, Date> cDate = typedCache();
        cDate.put(Date.valueOf("2018-02-18"), Date.valueOf("2017-01-01"));
        cDate.put(Date.valueOf("2018-01-01"), Date.valueOf("2017-02-02"));
        getTypedValue("Date", "2018-02-18", "2017-01-01");
        getTypedValue("java.sql.Date", "2018-01-01", "2017-02-02");
        // Test time type.
        IgniteCache<Time, Time> cTime = typedCache();
        cTime.put(Time.valueOf("01:01:01"), Time.valueOf("02:02:02"));
        cTime.put(Time.valueOf("03:03:03"), Time.valueOf("04:04:04"));
        getTypedValue("Time", "01:01:01", "02:02:02");
        getTypedValue("java.sql.Time", "03:03:03", "04:04:04");
        // Test timestamp type.
        IgniteCache<Timestamp, String> cTimestamp = typedCache();
        cTimestamp.put(Timestamp.valueOf("2018-02-18 01:01:01"), "test1");
        cTimestamp.put(Timestamp.valueOf("2018-01-01 01:01:01"), "test2");
        getTypedValue("Timestamp", "2018-02-18%2001:01:01", "test1");
        getTypedValue("java.sql.timestamp", "2018-01-01%2001:01:01", "test2");
        // Test UUID type.
        IgniteCache<UUID, UUID> cUUID = typedCache();
        UUID k1 = UUID.fromString("121f5ae8-148d-11e8-b642-0ed5f89f718b");
        UUID v1 = UUID.fromString("64c6c225-b31c-4000-b136-ef14562ac785");
        cUUID.put(k1, v1);
        UUID k2 = UUID.randomUUID();
        UUID v2 = UUID.randomUUID();
        cUUID.put(k2, v2);
        getTypedValue("UUID", k1.toString(), v1.toString());
        getTypedValue("java.util.UUID", k2.toString(), v2.toString());
        // Test IgniteUuid type.
        IgniteCache<IgniteUuid, IgniteUuid> cIgniteUUID = typedCache();
        IgniteUuid ik1 = IgniteUuid.randomUuid();
        IgniteUuid iv1 = IgniteUuid.randomUuid();
        cIgniteUUID.put(ik1, iv1);
        IgniteUuid ik2 = IgniteUuid.randomUuid();
        IgniteUuid iv2 = IgniteUuid.randomUuid();
        cIgniteUUID.put(ik2, iv2);
        getTypedValue("IgniteUuid", ik1.toString(), iv1.toString());
        getTypedValue("org.apache.ignite.lang.IgniteUuid", ik2.toString(), iv2.toString());
        // Test tuple.
        IgniteCache<Integer, T2<Integer, String>> cTuple = typedCache();
        T2<Integer, String> tup = new T2(1, "test");
        cTuple.put(555, tup);
        getTypedValue("int", "555", JettyRestProcessorCommonSelfTest.JSON_MAPPER.writeValueAsString(tup));
        // Test enum.
        IgniteCache<Integer, CacheMode> cEnum = typedCache();
        cEnum.put(888, CacheMode.PARTITIONED);
        getTypedValue("int", "888", CacheMode.PARTITIONED.toString());
    }

    /**
     * Organization class.
     */
    public static class Organization implements Serializable {
        /**
         * Organization ID (indexed).
         */
        @QuerySqlField(index = true)
        private Integer id;

        /**
         * First name (not-indexed).
         */
        @QuerySqlField(index = true)
        private String name;

        /**
         *
         *
         * @param id
         * 		Id.
         * @param name
         * 		Name.
         */
        Organization(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        /**
         *
         *
         * @return Id.
         */
        public Integer getId() {
            return id;
        }

        /**
         *
         *
         * @return Name.
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Test class that could have circular references.
     */
    public static class CircularRef implements Serializable {
        /**
         *
         */
        private int id;

        /**
         *
         */
        private String name;

        /**
         *
         */
        private JettyRestProcessorAbstractSelfTest.CircularRef ref;

        /**
         *
         *
         * @param id
         * 		ID.
         * @param name
         * 		Name.
         */
        CircularRef(int id, String name) {
            this.id = id;
            this.name = name;
        }

        /**
         *
         *
         * @return ID.
         */
        public int id() {
            return id;
        }

        /**
         *
         *
         * @return Name.
         */
        public String name() {
            return name;
        }

        /**
         *
         *
         * @return Reference to other object.
         */
        public JettyRestProcessorAbstractSelfTest.CircularRef ref() {
            return ref;
        }

        /**
         *
         *
         * @param ref
         * 		Reference to other object.
         */
        public void ref(JettyRestProcessorAbstractSelfTest.CircularRef ref) {
            this.ref = ref;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public String toString() {
            SB sb = new SB();
            sb.a('{').a('"').a("id").a('"').a(':').a(id).a(',').a('"').a("name").a('"').a(':').a('"').a(name).a('"').a(',').a('"').a("ref").a('"').a(':').a(ref).a('}');
            return sb.toString();
        }
    }

    /**
     * Person class.
     */
    public static class Person implements Serializable {
        /**
         * Person id.
         */
        private static int PERSON_ID = 0;

        /**
         * Person ID (indexed).
         */
        @QuerySqlField(index = true)
        private Integer id;

        /**
         * Organization id.
         */
        @QuerySqlField(index = true)
        private Integer orgId;

        /**
         * First name (not-indexed).
         */
        @QuerySqlField
        private String firstName;

        /**
         * Last name (not indexed).
         */
        @QuerySqlField
        private String lastName;

        /**
         * Salary (indexed).
         */
        @QuerySqlField(index = true)
        private double salary;

        /**
         *
         *
         * @param firstName
         * 		First name.
         * @param lastName
         * 		Last name.
         * @param salary
         * 		Salary.
         */
        Person(Integer orgId, String firstName, String lastName, double salary) {
            id = (JettyRestProcessorAbstractSelfTest.Person.PERSON_ID)++;
            this.orgId = orgId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.salary = salary;
        }

        /**
         *
         *
         * @return Organization ID.
         */
        public Integer getOrganizationId() {
            return orgId;
        }

        /**
         *
         *
         * @return First name.
         */
        public String getFirstName() {
            return firstName;
        }

        /**
         *
         *
         * @return Last name.
         */
        public String getLastName() {
            return lastName;
        }

        /**
         *
         *
         * @return Salary.
         */
        public double getSalary() {
            return salary;
        }

        /**
         *
         *
         * @return Id.
         */
        public Integer getId() {
            return id;
        }
    }

    /**
     * Test filter for scan query.
     */
    public static class ScanFilter implements IgniteBiPredicate<Integer, JettyRestProcessorAbstractSelfTest.Person> {
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean apply(Integer integer, JettyRestProcessorAbstractSelfTest.Person person) {
            return (person.salary) > 1000;
        }
    }

    /**
     * Filter by node ID.
     */
    private static class NodeIdFilter implements IgnitePredicate<ClusterNode> {
        /**
         *
         */
        private final UUID nid;

        /**
         *
         *
         * @param nid
         * 		Node ID where cache should be started.
         */
        NodeIdFilter(UUID nid) {
            this.nid = nid;
        }

        /**
         * {@inheritDoc }
         */
        @Override
        public boolean apply(ClusterNode n) {
            return n.id().equals(nid);
        }
    }

    /**
     * Helper for build {@link VisorGatewayTask} arguments.
     */
    public static class VisorGatewayArgument extends HashMap<String, String> {
        /**
         * Latest argument index.
         */
        private int idx = 3;

        /**
         * Construct helper object.
         *
         * @param cls
         * 		Class of executed task.
         */
        public VisorGatewayArgument(Class cls) {
            super(F.asMap("cmd", EXE.key(), "name", VisorGatewayTask.class.getName(), "p1", "null", "p2", cls.getName()));
        }

        /**
         * Execute task on node.
         *
         * @param node
         * 		Node.
         * @return This helper for chaining method calls.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument forNode(ClusterNode node) {
            put("p1", (node != null ? node.id().toString() : null));
            return this;
        }

        /**
         * Prepare list of node IDs.
         *
         * @param nodes
         * 		Collection of nodes.
         * @return This helper for chaining method calls.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument forNodes(Collection<ClusterNode> nodes) {
            put("p1", JettyRestProcessorAbstractSelfTest.VisorGatewayArgument.concat(F.transform(nodes, new org.apache.ignite.internal.util.typedef.C1<ClusterNode, UUID>() {
                /**
                 * {@inheritDoc }
                 */
                @Override
                public UUID apply(ClusterNode node) {
                    return node.id();
                }
            }).toArray(), ";"));
            return this;
        }

        /**
         * Add custom argument.
         *
         * @param vals
         * 		Values.
         * @return This helper for chaining method calls.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument arguments(Object... vals) {
            for (Object val : vals)
                put(("p" + ((idx)++)), String.valueOf(val));

            return this;
        }

        /**
         * Add string argument.
         *
         * @param val
         * 		Value.
         * @return This helper for chaining method calls.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument argument(String val) {
            put(("p" + ((idx)++)), String.class.getName());
            put(("p" + ((idx)++)), val);
            return this;
        }

        /**
         * Add custom class argument.
         *
         * @param cls
         * 		Class.
         * @param vals
         * 		Values.
         * @return This helper for chaining method calls.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument argument(Class cls, Object... vals) {
            put(("p" + ((idx)++)), cls.getName());
            for (Object val : vals)
                put(("p" + ((idx)++)), (val != null ? val.toString() : null));

            return this;
        }

        /**
         * Add collection argument.
         *
         * @param cls
         * 		Class.
         * @param vals
         * 		Values.
         * @return This helper for chaining method calls.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument collection(Class cls, Object... vals) {
            put(("p" + ((idx)++)), Collection.class.getName());
            put(("p" + ((idx)++)), cls.getName());
            put(("p" + ((idx)++)), JettyRestProcessorAbstractSelfTest.VisorGatewayArgument.concat(vals, ";"));
            return this;
        }

        /**
         * Add set argument.
         *
         * @param cls
         * 		Class.
         * @param vals
         * 		Values.
         * @return This helper for chaining method calls.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument set(Class cls, Object... vals) {
            put(("p" + ((idx)++)), Set.class.getName());
            put(("p" + ((idx)++)), cls.getName());
            put(("p" + ((idx)++)), JettyRestProcessorAbstractSelfTest.VisorGatewayArgument.concat(vals, ";"));
            return this;
        }

        /**
         * Add map argument.
         *
         * @param keyCls
         * 		Key class.
         * @param valCls
         * 		Value class.
         * @param map
         * 		Map.
         */
        public JettyRestProcessorAbstractSelfTest.VisorGatewayArgument map(Class keyCls, Class valCls, Map<?, ?> map) throws UnsupportedEncodingException {
            put(("p" + ((idx)++)), Map.class.getName());
            put(("p" + ((idx)++)), keyCls.getName());
            put(("p" + ((idx)++)), valCls.getName());
            SB sb = new SB();
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first)
                    sb.a(";");

                sb.a(entry.getKey());
                if ((entry.getValue()) != null)
                    sb.a("=").a(entry.getValue());

                first = false;
            }
            put(("p" + ((idx)++)), URLEncoder.encode(sb.toString(), JettyRestProcessorAbstractSelfTest.CHARSET));
            return this;
        }

        /**
         * Concat object with delimiter.
         *
         * @param vals
         * 		Values.
         * @param delim
         * 		Delimiter.
         */
        private static String concat(Object[] vals, String delim) {
            SB sb = new SB();
            boolean first = true;
            for (Object val : vals) {
                if (!first)
                    sb.a(delim);

                sb.a(val);
                first = false;
            }
            return sb.toString();
        }
    }
}
