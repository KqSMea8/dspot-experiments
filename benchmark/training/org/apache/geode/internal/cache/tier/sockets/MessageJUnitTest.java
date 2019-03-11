/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache.tier.sockets;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import org.apache.geode.test.junit.categories.ClientServerTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import static Message.DEFAULT_MAX_MESSAGE_SIZE;


@Category({ ClientServerTest.class })
public class MessageJUnitTest {
    private Message message;

    @Test
    public void clearDoesNotThrowNPE() throws Exception {
        // unsetComms clears the message's ByteBuffer, which was causing an NPE during shutdown
        // when clear() was invoked
        this.message.unsetComms();
        this.message.clear();
    }

    @Test
    public void numberOfPartsIsAdjusted() {
        int numParts = this.message.getNumberOfParts();
        this.message.setNumberOfParts(((2 * numParts) + 1));
        Assert.assertEquals(((2 * numParts) + 1), this.message.getNumberOfParts());
        this.message.addBytesPart(new byte[1]);
        this.message.addIntPart(2);
        this.message.addLongPart(3);
        this.message.addObjPart("4");
        this.message.addStringPart("5");
        Assert.assertEquals(5, this.message.getNextPartNumber());
    }

    @Test
    public void messageLongerThanMaxIntIsRejected() throws Exception {
        Part mockPart1 = Mockito.mock(Part.class);
        Mockito.when(mockPart1.getLength()).thenReturn(((Integer.MAX_VALUE) / 2));
        Part[] parts = new Part[2];
        parts[0] = mockPart1;
        parts[1] = mockPart1;
        this.message.setParts(parts);
        try {
            this.message.send();
            Assert.fail("expected an exception but none was thrown");
        } catch (MessageTooLargeException e) {
            Assert.assertTrue(e.getMessage().contains("exceeds maximum integer value"));
        }
    }

    @Test
    public void maxMessageSizeIsRespected() throws Exception {
        Part mockPart1 = Mockito.mock(Part.class);
        Mockito.when(mockPart1.getLength()).thenReturn(((DEFAULT_MAX_MESSAGE_SIZE) / 2));
        Part[] parts = new Part[2];
        parts[0] = mockPart1;
        parts[1] = mockPart1;
        this.message.setParts(parts);
        try {
            this.message.send();
            Assert.fail("expected an exception but none was thrown");
        } catch (MessageTooLargeException e) {
            Assert.assertFalse(e.getMessage().contains("exceeds maximum integer value"));
        }
    }

    /**
     * geode-1468: Message should clear the chunks in its Parts when performing cleanup.
     */
    @Test
    public void streamBuffersAreClearedDuringCleanup() throws Exception {
        Part mockPart1 = Mockito.mock(Part.class);
        Mockito.when(mockPart1.getLength()).thenReturn(100);
        Part[] parts = new Part[2];
        parts[0] = mockPart1;
        parts[1] = mockPart1;
        this.message.setParts(parts);
        this.message.clearParts();
        Mockito.verify(mockPart1, Mockito.times(2)).clear();
    }

    /**
     * Client subscription threads establish a timeout when reading a message header in order to avoid
     * hanging should the server's machine fail, or should the network path to the server have
     * problems. This test ensures that the method Message.receiveWithHeaderReadTimeout correctly
     * times out when trying to read a message header.
     *
     * @see ClientServerMiscDUnitTest#testClientReceivesPingIntervalSetting
     */
    @Test(expected = SocketTimeoutException.class)
    public void messageWillTimeoutDuringRecvOnInactiveSocket() throws Exception {
        final ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        Thread serverThread = new Thread("acceptor thread") {
            @Override
            public void run() {
                Socket client = null;
                try {
                    client = serverSocket.accept();
                    Thread.sleep(12000);
                } catch (InterruptedException e) {
                } catch (IOException e) {
                } finally {
                    if ((client != null) && (!(client.isClosed()))) {
                        try {
                            client.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        };
        serverThread.setDaemon(true);
        serverThread.start();
        try {
            Socket socket = new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort());
            MessageStats messageStats = Mockito.mock(MessageStats.class);
            message.setComms(socket, ByteBuffer.allocate(100), messageStats);
            message.receiveWithHeaderReadTimeout(500);
        } finally {
            serverThread.interrupt();
            if ((serverSocket != null) && (!(serverSocket.isClosed()))) {
                serverSocket.close();
            }
        }
    }
}
