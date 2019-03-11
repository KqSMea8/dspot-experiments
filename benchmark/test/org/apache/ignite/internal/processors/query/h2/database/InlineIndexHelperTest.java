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
package org.apache.ignite.internal.processors.query.h2.database;


import PageIdAllocator.FLAG_DATA;
import ValueNull.INSTANCE;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.io.Charsets;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.internal.pagemem.PageMemory;
import org.apache.ignite.internal.processors.cache.index.AbstractIndexingCommonTest;
import org.h2.result.SortOrder;
import org.h2.value.CompareMode;
import org.h2.value.Value;
import org.h2.value.ValueBoolean;
import org.h2.value.ValueByte;
import org.h2.value.ValueBytes;
import org.h2.value.ValueDate;
import org.h2.value.ValueDouble;
import org.h2.value.ValueFloat;
import org.h2.value.ValueInt;
import org.h2.value.ValueJavaObject;
import org.h2.value.ValueLong;
import org.h2.value.ValueShort;
import org.h2.value.ValueString;
import org.h2.value.ValueTime;
import org.h2.value.ValueTimestamp;
import org.h2.value.ValueUuid;
import org.junit.Test;


/**
 * Simple tests for {@link InlineIndexHelper}.
 */
public class InlineIndexHelperTest extends AbstractIndexingCommonTest {
    /**
     *
     */
    private static final int CACHE_ID = 42;

    /**
     *
     */
    private static final int PAGE_SIZE = 1024;

    /**
     *
     */
    private static final long MB = 1024;

    /**
     *
     */
    private static final Comparator ALWAYS_FAILS_COMPARATOR = new InlineIndexHelperTest.AlwaysFailsComparator();

    /**
     * Test utf-8 string cutting.
     */
    @Test
    public void testConvert() {
        // 8 bytes total: 1b, 1b, 3b, 3b.
        byte[] bytes = InlineIndexHelper.trimUTF8("00\u20ac\u20ac".getBytes(Charsets.UTF_8), 7);
        assertEquals(5, bytes.length);
        String s = new String(bytes);
        assertEquals(3, s.length());
        bytes = InlineIndexHelper.trimUTF8("aaaaaa".getBytes(Charsets.UTF_8), 4);
        assertEquals(4, bytes.length);
    }

    /**
     *
     */
    @Test
    public void testCompare1bytes() throws Exception {
        int maxSize = 3 + 2;// 2 ascii chars + 3 bytes header.

        assertEquals(0, putAndCompare("aa", "aa", maxSize));
        assertEquals((-1), putAndCompare("aa", "bb", maxSize));
        assertEquals((-1), putAndCompare("aaa", "bbb", maxSize));
        assertEquals(1, putAndCompare("bbb", "aaa", maxSize));
        assertEquals(1, putAndCompare("aaa", "aa", maxSize));
        assertEquals(1, putAndCompare("aaa", "a", maxSize));
        assertEquals(InlineIndexHelper.CANT_BE_COMPARE, putAndCompare("aaa", "aaa", maxSize));
        assertEquals(InlineIndexHelper.CANT_BE_COMPARE, putAndCompare("aaa", "aab", maxSize));
        assertEquals(InlineIndexHelper.CANT_BE_COMPARE, putAndCompare("aab", "aaa", maxSize));
    }

    /**
     *
     */
    @Test
    public void testCompare2bytes() throws Exception {
        int maxSize = 3 + 4;// 2 2-bytes chars + 3 bytes header.

        assertEquals(0, putAndCompare("??", "??", maxSize));
        assertEquals((-1), putAndCompare("??", "??", maxSize));
        assertEquals((-1), putAndCompare("???", "???", maxSize));
        assertEquals(1, putAndCompare("???", "???", maxSize));
        assertEquals(1, putAndCompare("???", "??", maxSize));
        assertEquals(1, putAndCompare("???", "?", maxSize));
        assertEquals(InlineIndexHelper.CANT_BE_COMPARE, putAndCompare("???", "???", maxSize));
        assertEquals(InlineIndexHelper.CANT_BE_COMPARE, putAndCompare("???", "???", maxSize));
        assertEquals(InlineIndexHelper.CANT_BE_COMPARE, putAndCompare("???", "???", maxSize));
    }

    /**
     *
     */
    @Test
    public void testCompare3bytes() throws Exception {
        int maxSize = 3 + 6;// 2 3-bytes chars + 3 bytes header.

        assertEquals(0, putAndCompare("??", "??", maxSize));
        assertEquals((-1), putAndCompare("??", "??", maxSize));
        assertEquals((-1), putAndCompare("???", "???", maxSize));
        assertEquals(1, putAndCompare("???", "???", maxSize));
        assertEquals(1, putAndCompare("???", "??", maxSize));
        assertEquals(1, putAndCompare("???", "?", maxSize));
        assertEquals((-2), putAndCompare("???", "???", maxSize));
        assertEquals((-2), putAndCompare("???", "???", maxSize));
        assertEquals((-2), putAndCompare("???", "???", maxSize));
    }

    /**
     *
     */
    @Test
    public void testCompare4bytes() throws Exception {
        int maxSize = 3 + 8;// 2 4-bytes chars + 3 bytes header.

        assertEquals(0, putAndCompare("\ud802\udd20\ud802\udd20", "\ud802\udd20\ud802\udd20", maxSize));
        assertEquals((-1), putAndCompare("\ud802\udd20\ud802\udd20", "\ud802\udd21\ud802\udd21", maxSize));
        assertEquals((-1), putAndCompare("\ud802\udd20\ud802\udd20\ud802\udd20", "\ud802\udd21\ud802\udd21\ud802\udd21", maxSize));
        assertEquals(1, putAndCompare("\ud802\udd21\ud802\udd21\ud802\udd21", "\ud802\udd20\ud802\udd20\ud802\udd20", maxSize));
        assertEquals(1, putAndCompare("\ud802\udd20\ud802\udd20\ud802\udd20", "\ud802\udd20\ud802\udd20", maxSize));
        assertEquals(1, putAndCompare("\ud802\udd20\ud802\udd20\ud802\udd20", "\ud802\udd20", maxSize));
        assertEquals((-2), putAndCompare("\ud802\udd20\ud802\udd20\ud802\udd20", "\ud802\udd20\ud802\udd20\ud802\udd20", maxSize));
        assertEquals((-2), putAndCompare("\ud802\udd20\ud802\udd20\ud802\udd20", "\ud802\udd20\ud802\udd20\ud802\udd21", maxSize));
        assertEquals((-2), putAndCompare("\ud802\udd20\ud802\udd20\ud802\udd21", "\ud802\udd20\ud802\udd20\ud802\udd20", maxSize));
    }

    /**
     *
     */
    @Test
    public void testCompareMixed() throws Exception {
        int maxSize = 3 + 8;// 2 up to 4-bytes chars + 3 bytes header.

        assertEquals(0, putAndCompare("\ud802\udd20\u0904", "\ud802\udd20\u0904", maxSize));
        assertEquals((-1), putAndCompare("\ud802\udd20\u0904", "\ud802\udd20\u0905", maxSize));
        assertEquals(1, putAndCompare("\u0905\ud802\udd20", "\u0904\ud802\udd20", maxSize));
        assertEquals((-2), putAndCompare("\ud802\udd20\ud802\udd20\u0905", "\ud802\udd20\ud802\udd20\u0904", maxSize));
    }

    /**
     *
     */
    @Test
    public void testCompareMixed2() throws Exception {
        int strCnt = 1000;
        int symbCnt = 20;
        int inlineSize = (symbCnt * 4) + 3;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        String[] strings = new String[strCnt];
        for (int i = 0; i < (strings.length); i++)
            strings[i] = randomString(symbCnt);

        Arrays.sort(strings);
        for (int i = 0; i < 100; i++) {
            int i1 = rnd.nextInt(strings.length);
            int i2 = rnd.nextInt(strings.length);
            assertEquals(Integer.compare(i1, i2), putAndCompare(strings[i1], strings[i2], inlineSize));
        }
    }

    /**
     * Limit is too small to cut
     */
    @Test
    public void testStringCut() {
        // 6 bytes total: 3b, 3b.
        byte[] bytes = InlineIndexHelper.trimUTF8("\u20ac\u20ac".getBytes(Charsets.UTF_8), 2);
        assertNull(bytes);
    }

    /**
     * Test on String values compare
     */
    @Test
    public void testRelyOnCompare() {
        InlineIndexHelper ha = new InlineIndexHelper("", Value.STRING, 0, SortOrder.ASCENDING, CompareMode.getInstance(null, 0));
        // same size
        assertFalse(getRes(ha, "aabb", "aabb"));
        // second string is shorter
        assertTrue(getRes(ha, "aabb", "aac"));
        assertTrue(getRes(ha, "aabb", "aaa"));
        // second string is longer
        assertTrue(getRes(ha, "aabb", "aaaaaa"));
        assertFalse(getRes(ha, "aaa", "aaaaaa"));
        // one is null
        assertTrue(getRes(ha, "a", null));
        assertTrue(getRes(ha, null, "a"));
        assertTrue(getRes(ha, null, null));
    }

    /**
     * Test on Bytes values compare
     */
    @Test
    public void testRelyOnCompareBytes() {
        InlineIndexHelper ha = new InlineIndexHelper("", Value.BYTES, 0, SortOrder.ASCENDING, CompareMode.getInstance(null, 0));
        // same size
        assertFalse(getResBytes(ha, new byte[]{ 1, 2, 3, 4 }, new byte[]{ 1, 2, 3, 4 }));
        // second aray is shorter
        assertTrue(getResBytes(ha, new byte[]{ 1, 2, 2, 2 }, new byte[]{ 1, 1, 2 }));
        assertTrue(getResBytes(ha, new byte[]{ 1, 1, 1, 2 }, new byte[]{ 1, 1, 2 }));
        // second array is longer
        assertTrue(getResBytes(ha, new byte[]{ 1, 2 }, new byte[]{ 1, 1, 1 }));
        assertFalse(getResBytes(ha, new byte[]{ 1, 1 }, new byte[]{ 1, 1, 2 }));
        // one is null
        assertTrue(getResBytes(ha, new byte[]{ 1, 2, 3, 4 }, null));
        assertTrue(getResBytes(ha, null, new byte[]{ 1, 2, 3, 4 }));
        assertTrue(getResBytes(ha, null, null));
    }

    /**
     * Test on Bytes values compare
     */
    @Test
    public void testRelyOnCompareJavaObject() {
        InlineIndexHelper ha = new InlineIndexHelper("", Value.JAVA_OBJECT, 0, SortOrder.ASCENDING, CompareMode.getInstance(null, 0));
        // different types
        assertTrue(getResJavaObjects(ha, new String("1234"), new Integer(10)));
        // the same types, but different values
        assertTrue(getResJavaObjects(ha, new String("1234"), new String("123")));
        // the same types and values
        assertFalse(getResJavaObjects(ha, new String("1234"), new String("1234")));
        // the same object
        String key = "1";
        assertFalse(getResJavaObjects(ha, key, key));
        // one is null
        assertTrue(getResJavaObjects(ha, key, null));
        assertTrue(getResJavaObjects(ha, null, key));
        assertTrue(getResJavaObjects(ha, null, null));
    }

    /**
     *
     */
    @Test
    public void testStringTruncate() throws Exception {
        DataRegionConfiguration plcCfg = new DataRegionConfiguration().setInitialSize((1024 * (InlineIndexHelperTest.MB))).setMaxSize((1024 * (InlineIndexHelperTest.MB)));
        PageMemory pageMem = new org.apache.ignite.internal.pagemem.impl.PageMemoryNoStoreImpl(log(), new org.apache.ignite.internal.mem.unsafe.UnsafeMemoryProvider(log()), null, InlineIndexHelperTest.PAGE_SIZE, plcCfg, new org.apache.ignite.internal.processors.cache.persistence.DataRegionMetricsImpl(plcCfg), false);
        pageMem.start();
        long pageId = 0L;
        long page = 0L;
        try {
            pageId = pageMem.allocatePage(InlineIndexHelperTest.CACHE_ID, 1, FLAG_DATA);
            page = pageMem.acquirePage(InlineIndexHelperTest.CACHE_ID, pageId);
            long pageAddr = pageMem.readLock(InlineIndexHelperTest.CACHE_ID, pageId, page);
            int off = 0;
            InlineIndexHelper ih = new InlineIndexHelper("", Value.STRING, 1, 0, CompareMode.getInstance(null, 0));
            ih.put(pageAddr, off, ValueString.get("aaaaaaa"), (3 + 5));
            assertFalse(ih.isValueFull(pageAddr, off));
            assertEquals("aaaaa", ih.get(pageAddr, off, (3 + 5)).getString());
            ih.put(pageAddr, off, ValueString.get("aaa"), (3 + 5));
            assertTrue(ih.isValueFull(pageAddr, off));
            assertEquals("aaa", ih.get(pageAddr, off, (3 + 5)).getString());
            ih.put(pageAddr, off, ValueString.get("\u20acaaa"), (3 + 2));
            assertNull(ih.get(pageAddr, off, (3 + 2)));
        } finally {
            if (page != 0L)
                pageMem.releasePage(InlineIndexHelperTest.CACHE_ID, pageId, page);

            pageMem.stop(true);
        }
    }

    /**
     *
     */
    @Test
    public void testBytes() throws Exception {
        DataRegionConfiguration plcCfg = new DataRegionConfiguration().setInitialSize((1024 * (InlineIndexHelperTest.MB))).setMaxSize((1024 * (InlineIndexHelperTest.MB)));
        PageMemory pageMem = new org.apache.ignite.internal.pagemem.impl.PageMemoryNoStoreImpl(log(), new org.apache.ignite.internal.mem.unsafe.UnsafeMemoryProvider(log()), null, InlineIndexHelperTest.PAGE_SIZE, plcCfg, new org.apache.ignite.internal.processors.cache.persistence.DataRegionMetricsImpl(plcCfg), false);
        pageMem.start();
        long pageId = 0L;
        long page = 0L;
        try {
            pageId = pageMem.allocatePage(InlineIndexHelperTest.CACHE_ID, 1, FLAG_DATA);
            page = pageMem.acquirePage(InlineIndexHelperTest.CACHE_ID, pageId);
            long pageAddr = pageMem.readLock(InlineIndexHelperTest.CACHE_ID, pageId, page);
            int off = 0;
            InlineIndexHelper ih = new InlineIndexHelper("", Value.BYTES, 1, 0, CompareMode.getInstance(null, 0));
            int maxSize = 3 + 3;
            int savedBytesCnt = ih.put(pageAddr, off, ValueBytes.get(new byte[]{ 1, 2, 3, 4, 5 }), maxSize);
            assertTrue((savedBytesCnt > 0));
            assertTrue((savedBytesCnt <= maxSize));
            assertFalse(ih.isValueFull(pageAddr, off));
            maxSize = 3 + 5;
            assertTrue(Arrays.equals(new byte[]{ 1, 2, 3 }, ih.get(pageAddr, off, maxSize).getBytes()));
            savedBytesCnt = ih.put(pageAddr, off, ValueBytes.get(new byte[]{ 1, 2, 3, 4, 5 }), maxSize);
            assertTrue((savedBytesCnt > 0));
            assertTrue((savedBytesCnt <= maxSize));
            assertTrue(ih.isValueFull(pageAddr, off));
            assertTrue(Arrays.equals(new byte[]{ 1, 2, 3, 4, 5 }, ih.get(pageAddr, off, maxSize).getBytes()));
        } finally {
            if (page != 0L)
                pageMem.releasePage(InlineIndexHelperTest.CACHE_ID, pageId, page);

            pageMem.stop(true);
        }
    }

    /**
     *
     */
    @Test
    public void testJavaObject() throws Exception {
        DataRegionConfiguration plcCfg = new DataRegionConfiguration().setInitialSize((1024 * (InlineIndexHelperTest.MB))).setMaxSize((1024 * (InlineIndexHelperTest.MB)));
        PageMemory pageMem = new org.apache.ignite.internal.pagemem.impl.PageMemoryNoStoreImpl(log(), new org.apache.ignite.internal.mem.unsafe.UnsafeMemoryProvider(log()), null, InlineIndexHelperTest.PAGE_SIZE, plcCfg, new org.apache.ignite.internal.processors.cache.persistence.DataRegionMetricsImpl(plcCfg), false);
        pageMem.start();
        long pageId = 0L;
        long page = 0L;
        try {
            pageId = pageMem.allocatePage(InlineIndexHelperTest.CACHE_ID, 1, FLAG_DATA);
            page = pageMem.acquirePage(InlineIndexHelperTest.CACHE_ID, pageId);
            long pageAddr = pageMem.readLock(InlineIndexHelperTest.CACHE_ID, pageId, page);
            int off = 0;
            InlineIndexHelper ih = new InlineIndexHelper("", Value.JAVA_OBJECT, 1, 0, CompareMode.getInstance(null, 0));
            int maxSize = 3 + 3;
            int savedBytesCnt = ih.put(pageAddr, off, ValueJavaObject.getNoCopy(null, new byte[]{ 1, 2, 3, 4, 5 }, null), maxSize);
            assertTrue((savedBytesCnt > 0));
            assertTrue((savedBytesCnt <= maxSize));
            assertFalse(ih.isValueFull(pageAddr, off));
            maxSize = 3 + 5;
            assertTrue(Arrays.equals(new byte[]{ 1, 2, 3 }, ih.get(pageAddr, off, maxSize).getBytes()));
            savedBytesCnt = ih.put(pageAddr, off, ValueJavaObject.getNoCopy(null, new byte[]{ 1, 2, 3, 4, 5 }, null), maxSize);
            assertTrue((savedBytesCnt > 0));
            assertTrue((savedBytesCnt <= maxSize));
            assertTrue(ih.isValueFull(pageAddr, off));
            assertTrue(Arrays.equals(new byte[]{ 1, 2, 3, 4, 5 }, ih.get(pageAddr, off, maxSize).getBytes()));
        } finally {
            if (page != 0L)
                pageMem.releasePage(InlineIndexHelperTest.CACHE_ID, pageId, page);

            pageMem.stop(true);
        }
    }

    /**
     *
     */
    @Test
    public void testNull() throws Exception {
        testPutGet(ValueInt.get((-1)), INSTANCE, ValueInt.get(3));
        testPutGet(ValueInt.get((-1)), INSTANCE, ValueInt.get(3));
        int maxSize = 3 + 2;// 2 ascii chars + 3 bytes header.

        assertEquals(1, putAndCompare("aa", null, maxSize));
    }

    /**
     *
     */
    @Test
    public void testBoolean() throws Exception {
        testPutGet(ValueBoolean.get(true), ValueBoolean.get(false), ValueBoolean.get(true));
    }

    /**
     *
     */
    @Test
    public void testByte() throws Exception {
        testPutGet(ValueByte.get(((byte) (-1))), ValueByte.get(((byte) (2))), ValueByte.get(((byte) (3))));
    }

    /**
     *
     */
    @Test
    public void testShort() throws Exception {
        testPutGet(ValueShort.get(((short) (-32000))), ValueShort.get(((short) (2))), ValueShort.get(((short) (3))));
    }

    /**
     *
     */
    @Test
    public void testInt() throws Exception {
        testPutGet(ValueInt.get((-1)), ValueInt.get(2), ValueInt.get(3));
    }

    /**
     *
     */
    @Test
    public void testLong() throws Exception {
        testPutGet(ValueLong.get((-1)), ValueLong.get(2), ValueLong.get(3));
    }

    /**
     *
     */
    @Test
    public void testFloat() throws Exception {
        testPutGet(ValueFloat.get(1.1F), ValueFloat.get(2.2F), ValueFloat.get(1.1F));
    }

    /**
     *
     */
    @Test
    public void testDouble() throws Exception {
        testPutGet(ValueDouble.get(1.1F), ValueDouble.get(2.2F), ValueDouble.get(1.1F));
    }

    /**
     *
     */
    @Test
    public void testDate() throws Exception {
        testPutGet(ValueDate.get(Date.valueOf("2017-02-20")), ValueDate.get(Date.valueOf("2017-02-21")), ValueDate.get(Date.valueOf("2017-02-19")));
    }

    /**
     *
     */
    @Test
    public void testTime() throws Exception {
        testPutGet(ValueTime.get(Time.valueOf("10:01:01")), ValueTime.get(Time.valueOf("11:02:02")), ValueTime.get(Time.valueOf("12:03:03")));
    }

    /**
     *
     */
    @Test
    public void testTimestamp() throws Exception {
        testPutGet(ValueTimestamp.get(Timestamp.valueOf("2017-02-20 10:01:01")), ValueTimestamp.get(Timestamp.valueOf("2017-02-20 10:01:01")), ValueTimestamp.get(Timestamp.valueOf("2017-02-20 10:01:01")));
    }

    /**
     *
     */
    @Test
    public void testUUID() throws Exception {
        testPutGet(ValueUuid.get(UUID.randomUUID().toString()), ValueUuid.get(UUID.randomUUID().toString()), ValueUuid.get(UUID.randomUUID().toString()));
    }

    /**
     *
     */
    private static class AlwaysFailsComparator implements Comparator<Value> {
        /**
         * {@inheritDoc }
         */
        @Override
        public int compare(Value o1, Value o2) {
            throw new AssertionError("Optimized algorithm should be used.");
        }
    }
}
