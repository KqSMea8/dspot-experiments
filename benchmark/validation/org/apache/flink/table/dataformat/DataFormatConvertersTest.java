/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.table.dataformat;


import BasicArrayTypeInfo.DOUBLE_ARRAY_TYPE_INFO;
import Types.BIG_DEC;
import Types.STRING;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.PrimitiveArrayTypeInfo;
import org.apache.flink.api.common.typeinfo.SqlTimeTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.ObjectArrayTypeInfo;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.table.dataformat.DataFormatConverters.DataFormatConverter;
import org.apache.flink.table.runtime.functions.DateTimeUtils;
import org.apache.flink.table.type.InternalTypes;
import org.apache.flink.table.typeutils.BinaryStringTypeInfo;
import org.apache.flink.table.typeutils.DecimalTypeInfo;
import org.apache.flink.types.Row;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test for {@link DataFormatConverters}.
 */
public class DataFormatConvertersTest {
    private TypeInformation[] simpleTypes = new TypeInformation[]{ BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.BOOLEAN_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO, BasicTypeInfo.LONG_TYPE_INFO, BasicTypeInfo.FLOAT_TYPE_INFO, BasicTypeInfo.DOUBLE_TYPE_INFO, BasicTypeInfo.SHORT_TYPE_INFO, BasicTypeInfo.BYTE_TYPE_INFO, BasicTypeInfo.CHAR_TYPE_INFO, PrimitiveArrayTypeInfo.BOOLEAN_PRIMITIVE_ARRAY_TYPE_INFO, PrimitiveArrayTypeInfo.INT_PRIMITIVE_ARRAY_TYPE_INFO, PrimitiveArrayTypeInfo.LONG_PRIMITIVE_ARRAY_TYPE_INFO, PrimitiveArrayTypeInfo.FLOAT_PRIMITIVE_ARRAY_TYPE_INFO, PrimitiveArrayTypeInfo.DOUBLE_PRIMITIVE_ARRAY_TYPE_INFO, PrimitiveArrayTypeInfo.SHORT_PRIMITIVE_ARRAY_TYPE_INFO, PrimitiveArrayTypeInfo.BYTE_PRIMITIVE_ARRAY_TYPE_INFO, PrimitiveArrayTypeInfo.CHAR_PRIMITIVE_ARRAY_TYPE_INFO, SqlTimeTypeInfo.DATE, SqlTimeTypeInfo.TIME, SqlTimeTypeInfo.TIMESTAMP, BinaryStringTypeInfo.INSTANCE };

    private Object[] simpleValues = new Object[]{ "haha", true, 22, 1111L, 0.5F, 0.5, ((short) (1)), ((byte) (1)), ((char) (1)), new boolean[]{ true, false }, new int[]{ 5, 1 }, new long[]{ 5, 1 }, new float[]{ 5, 1 }, new double[]{ 5, 1 }, new short[]{ 5, 1 }, new byte[]{ 5, 1 }, new char[]{ 5, 1 }, DateTimeUtils.internalToDate(5), new Time(11), new Timestamp(11), BinaryString.fromString("hahah") };

    @Test
    public void testTypes() {
        for (int i = 0; i < (simpleTypes.length); i++) {
            DataFormatConvertersTest.test(simpleTypes[i], simpleValues[i]);
        }
        DataFormatConvertersTest.test(new org.apache.flink.api.java.typeutils.RowTypeInfo(simpleTypes), new Row(simpleTypes.length));
        DataFormatConvertersTest.test(new org.apache.flink.api.java.typeutils.RowTypeInfo(simpleTypes), Row.of(simpleValues));
        DataFormatConvertersTest.test(new org.apache.flink.table.typeutils.BaseRowTypeInfo(InternalTypes.STRING, InternalTypes.INT), GenericRow.of(BinaryString.fromString("hehe"), 111));
        DataFormatConvertersTest.test(new org.apache.flink.table.typeutils.BaseRowTypeInfo(InternalTypes.STRING, InternalTypes.INT), GenericRow.of(null, null));
        DataFormatConvertersTest.test(new DecimalTypeInfo(10, 5), null);
        DataFormatConvertersTest.test(new DecimalTypeInfo(10, 5), Decimal.castFrom(5.555, 10, 5));
        DataFormatConvertersTest.test(BIG_DEC, null);
        {
            DataFormatConverter converter = DataFormatConverters.getConverterForTypeInfo(BIG_DEC);
            Assert.assertTrue(Arrays.deepEquals(new Object[]{ converter.toInternal(converter.toExternal(Decimal.castFrom(5, 19, 18))) }, new Object[]{ Decimal.castFrom(5, 19, 18) }));
        }
        DataFormatConvertersTest.test(new org.apache.flink.api.java.typeutils.ListTypeInfo(Types.STRING), null);
        DataFormatConvertersTest.test(new org.apache.flink.api.java.typeutils.ListTypeInfo(Types.STRING), Arrays.asList("ahah", "xx"));
        DataFormatConvertersTest.test(new org.apache.flink.table.typeutils.BinaryGenericTypeInfo(new org.apache.flink.table.type.GenericType(Types.STRING)), null);
        DataFormatConvertersTest.test(new org.apache.flink.table.typeutils.BinaryGenericTypeInfo(new org.apache.flink.table.type.GenericType(Types.STRING)), "hahaha");
        DataFormatConvertersTest.test(DOUBLE_ARRAY_TYPE_INFO, new Double[]{ 1.0, 5.0 });
        DataFormatConvertersTest.test(DOUBLE_ARRAY_TYPE_INFO, new Double[]{ null, null });
        DataFormatConvertersTest.test(ObjectArrayTypeInfo.getInfoFor(STRING), new String[]{ null, null });
        DataFormatConvertersTest.test(ObjectArrayTypeInfo.getInfoFor(STRING), new String[]{ "haha", "hehe" });
        DataFormatConvertersTest.test(new org.apache.flink.api.java.typeutils.MapTypeInfo(Types.STRING, Types.INT), null);
        HashMap<String, Integer> map = new HashMap<>();
        map.put("haha", 1);
        map.put("hah1", 5);
        map.put(null, null);
        DataFormatConvertersTest.test(new org.apache.flink.api.java.typeutils.MapTypeInfo(Types.STRING, Types.INT), map);
        Tuple2 tuple2 = new Tuple2(5, 10);
        TupleTypeInfo tupleTypeInfo = new TupleTypeInfo(tuple2.getClass(), Types.INT, Types.INT);
        DataFormatConvertersTest.test(tupleTypeInfo, tuple2);
        DataFormatConvertersTest.test(TypeExtractor.createTypeInfo(DataFormatConvertersTest.MyPojo.class), new DataFormatConvertersTest.MyPojo(1, 3));
        DataFormatConvertersTest.test(new org.apache.flink.table.typeutils.BinaryArrayTypeInfo(InternalTypes.INT), BinaryArray.fromPrimitiveArray(new int[]{ 1, 5 }));
        DataFormatConvertersTest.test(new org.apache.flink.table.typeutils.BinaryMapTypeInfo(InternalTypes.INT, InternalTypes.INT), BinaryMap.valueOf(BinaryArray.fromPrimitiveArray(new int[]{ 1, 5 }), BinaryArray.fromPrimitiveArray(new int[]{ 6, 7 })));
    }

    /**
     * Test pojo.
     */
    public static class MyPojo {
        public int f1 = 0;

        public int f2 = 0;

        public MyPojo() {
        }

        public MyPojo(int f1, int f2) {
            this.f1 = f1;
            this.f2 = f2;
        }

        @Override
        public boolean equals(Object o) {
            if ((this) == o) {
                return true;
            }
            if ((o == null) || ((getClass()) != (o.getClass()))) {
                return false;
            }
            DataFormatConvertersTest.MyPojo myPojo = ((DataFormatConvertersTest.MyPojo) (o));
            return ((f1) == (myPojo.f1)) && ((f2) == (myPojo.f2));
        }
    }
}
