/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.bson;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import org.apache.drill.exec.memory.BufferAllocator;
import org.apache.drill.exec.ops.BufferManager;
import org.apache.drill.exec.store.TestOutputMutator;
import org.apache.drill.exec.vector.complex.impl.SingleMapReaderImpl;
import org.apache.drill.exec.vector.complex.impl.VectorContainerWriter;
import org.apache.drill.exec.vector.complex.reader.FieldReader;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonWriter;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;


public class TestBsonRecordReader {
    private BufferAllocator allocator;

    private VectorContainerWriter writer;

    private TestOutputMutator mutator;

    private BufferManager bufferManager;

    private BsonRecordReader bsonReader;

    @Test
    public void testIntType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("seqNo", new BsonInt64(10));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals(10L, mapReader.reader("seqNo").readLong().longValue());
    }

    @Test
    public void testTimeStampType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("ts", new BsonTimestamp(1000, 10));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals(1000000L, mapReader.reader("ts").readLocalDateTime().atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli());
    }

    @Test
    public void testSymbolType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("symbolKey", new BsonSymbol("test_symbol"));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals("test_symbol", mapReader.reader("symbolKey").readText().toString());
    }

    @Test
    public void testStringType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("stringKey", new BsonString("test_string"));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals("test_string", mapReader.reader("stringKey").readText().toString());
    }

    @Test
    public void testSpecialCharStringType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("stringKey", new BsonString("?????????1"));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals("?????????1", mapReader.reader("stringKey").readText().toString());
    }

    @Test
    public void testObjectIdType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        BsonObjectId value = new BsonObjectId(new ObjectId());
        bsonDoc.append("_idKey", value);
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        byte[] readByteArray = mapReader.reader("_idKey").readByteArray();
        Assert.assertTrue(Arrays.equals(value.getValue().toByteArray(), readByteArray));
    }

    @Test
    public void testNullType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("nullKey", new BsonNull());
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals(null, mapReader.reader("nullKey").readObject());
    }

    @Test
    public void testDoubleType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("doubleKey", new BsonDouble(12.35));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals(12.35, mapReader.reader("doubleKey").readDouble().doubleValue(), 1.0E-5);
    }

    @Test
    public void testArrayOfDocumentType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        BsonWriter bw = new org.bson.BsonDocumentWriter(bsonDoc);
        bw.writeStartDocument();
        bw.writeName("a");
        bw.writeString("MongoDB");
        bw.writeName("b");
        bw.writeStartArray();
        bw.writeStartDocument();
        bw.writeName("c");
        bw.writeInt32(1);
        bw.writeEndDocument();
        bw.writeEndArray();
        bw.writeEndDocument();
        bw.flush();
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        FieldReader reader = writer.getMapVector().getReader();
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (reader));
        FieldReader reader3 = mapReader.reader("b");
        Assert.assertEquals("MongoDB", mapReader.reader("a").readText().toString());
    }

    @Test
    public void testRecursiveDocuments() throws IOException {
        BsonDocument topDoc = new BsonDocument();
        final int count = 3;
        for (int i = 0; i < count; ++i) {
            BsonDocument bsonDoc = new BsonDocument();
            BsonWriter bw = new org.bson.BsonDocumentWriter(bsonDoc);
            bw.writeStartDocument();
            bw.writeName(("k1" + i));
            bw.writeString(("drillMongo1" + i));
            bw.writeName(("k2" + i));
            bw.writeString(("drillMongo2" + i));
            bw.writeEndDocument();
            bw.flush();
            topDoc.append(("doc" + i), bsonDoc);
        }
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(topDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        for (int i = 0; i < count; ++i) {
            SingleMapReaderImpl reader = ((SingleMapReaderImpl) (mapReader.reader(("doc" + i))));
            Assert.assertEquals(("drillMongo1" + i), reader.reader(("k1" + i)).readText().toString());
            Assert.assertEquals(("drillMongo2" + i), reader.reader(("k2" + i)).readText().toString());
        }
    }

    @Test
    public void testDateTimeType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("dateTimeKey", new BsonDateTime(5262729712L));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertEquals(5262729712L, mapReader.reader("dateTimeKey").readLocalDateTime().atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli());
    }

    @Test
    public void testBooleanType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        bsonDoc.append("booleanKey", new BsonBoolean(true));
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertTrue(mapReader.reader("booleanKey").readBoolean());
    }

    @Test
    public void testBinaryTypes() throws IOException {
        // test with different binary types
        BsonDocument bsonDoc = new BsonDocument();
        // Binary
        // String
        byte[] bytes = "binaryValue".getBytes();
        bsonDoc.append("binaryKey", new BsonBinary(BsonBinarySubType.BINARY, bytes));
        // String
        byte[] bytesString = "binaryStringValue".getBytes();
        bsonDoc.append("binaryStringKey", new BsonBinary(((byte) (2)), bytesString));
        // Double
        byte[] bytesDouble = new byte[8];
        ByteBuffer.wrap(bytesDouble).putDouble(23.0123);
        BsonBinary bsonDouble = new BsonBinary(((byte) (1)), bytesDouble);
        bsonDoc.append("binaryDouble", bsonDouble);
        // Boolean
        byte[] booleanBytes = new byte[8];
        ByteBuffer.wrap(booleanBytes).put(((byte) (1)));
        BsonBinary bsonBoolean = new BsonBinary(((byte) (8)), booleanBytes);
        bsonDoc.append("bsonBoolean", bsonBoolean);
        writer.reset();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        Assert.assertTrue(Arrays.equals(bytes, mapReader.reader("binaryKey").readByteArray()));
        Assert.assertEquals("binaryStringValue", mapReader.reader("binaryStringKey").readText().toString());
        Assert.assertEquals(23.0123, mapReader.reader("binaryDouble").readDouble().doubleValue(), 0);
        FieldReader reader = mapReader.reader("bsonBoolean");
        Assert.assertEquals(true, reader.readBoolean().booleanValue());
    }

    @Test
    public void testArrayType() throws IOException {
        BsonDocument bsonDoc = new BsonDocument();
        BsonWriter bw = new org.bson.BsonDocumentWriter(bsonDoc);
        bw.writeStartDocument();
        bw.writeName("arrayKey");
        bw.writeStartArray();
        bw.writeInt32(1);
        bw.writeInt32(2);
        bw.writeInt32(3);
        bw.writeEndArray();
        bw.writeEndDocument();
        bw.flush();
        bsonReader.write(writer, new org.bson.BsonDocumentReader(bsonDoc));
        SingleMapReaderImpl mapReader = ((SingleMapReaderImpl) (writer.getMapVector().getReader()));
        FieldReader reader = mapReader.reader("arrayKey");
        Assert.assertEquals(3, reader.size());
    }
}
