/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zookeeper.server.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.jute.BinaryOutputArchive;
import org.apache.jute.OutputArchive;
import org.apache.jute.Record;
import org.apache.zookeeper.server.Request;
import org.apache.zookeeper.txn.TxnHeader;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


public class SerializeUtilsTest {
    @Test
    public void testSerializeRequestRequestIsNull() {
        byte[] data = SerializeUtils.serializeRequest(null);
        Assert.assertNull(data);
    }

    @Test
    public void testSerializeRequestRequestHeaderIsNull() {
        Request request = new Request(0, 0, 0, null, null, 0);
        byte[] data = SerializeUtils.serializeRequest(request);
        Assert.assertNull(data);
    }

    @Test
    public void testSerializeRequestWithoutTxn() throws IOException {
        // Arrange
        TxnHeader header = Mockito.mock(TxnHeader.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                OutputArchive oa = ((OutputArchive) (args[0]));
                oa.writeString("header", "test");
                return null;
            }
        }).when(header).serialize(ArgumentMatchers.any(OutputArchive.class), ArgumentMatchers.anyString());
        Request request = new Request(1, 2, 3, header, null, 4);
        // Act
        byte[] data = SerializeUtils.serializeRequest(request);
        // Assert
        Assert.assertNotNull(data);
        Mockito.verify(header).serialize(ArgumentMatchers.any(OutputArchive.class), ArgumentMatchers.eq("hdr"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryOutputArchive boa = BinaryOutputArchive.getArchive(baos);
        boa.writeString("header", "test");
        baos.close();
        Assert.assertArrayEquals(baos.toByteArray(), data);
    }

    @Test
    public void testSerializeRequestWithTxn() throws IOException {
        // Arrange
        TxnHeader header = Mockito.mock(TxnHeader.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                OutputArchive oa = ((OutputArchive) (args[0]));
                oa.writeString("header", "test");
                return null;
            }
        }).when(header).serialize(ArgumentMatchers.any(OutputArchive.class), ArgumentMatchers.anyString());
        Record txn = Mockito.mock(Record.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                OutputArchive oa = ((OutputArchive) (args[0]));
                oa.writeString("record", "test");
                return null;
            }
        }).when(txn).serialize(ArgumentMatchers.any(OutputArchive.class), ArgumentMatchers.anyString());
        Request request = new Request(1, 2, 3, header, txn, 4);
        // Act
        byte[] data = SerializeUtils.serializeRequest(request);
        // Assert
        Assert.assertNotNull(data);
        InOrder inOrder = Mockito.inOrder(header, txn);
        inOrder.verify(header).serialize(ArgumentMatchers.any(OutputArchive.class), ArgumentMatchers.eq("hdr"));
        inOrder.verify(txn).serialize(ArgumentMatchers.any(OutputArchive.class), ArgumentMatchers.eq("txn"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryOutputArchive boa = BinaryOutputArchive.getArchive(baos);
        boa.writeString("header", "test");
        boa.writeString("record", "test");
        baos.close();
        Assert.assertArrayEquals(baos.toByteArray(), data);
    }
}
