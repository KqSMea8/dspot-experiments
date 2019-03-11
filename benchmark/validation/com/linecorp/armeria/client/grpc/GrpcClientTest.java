/**
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.client.grpc;


import CallOptions.DEFAULT;
import ClientOption.DEFAULT_RESPONSE_TIMEOUT_MILLIS;
import ClientOption.HTTP_HEADERS;
import Code.CANCELLED;
import Code.RESOURCE_EXHAUSTED;
import GrpcClientOptions.MAX_INBOUND_MESSAGE_SIZE_BYTES;
import GrpcClientOptions.MAX_OUTBOUND_MESSAGE_SIZE_BYTES;
import GrpcClientOptions.UNSAFE_WRAP_RESPONSE_BUFFERS;
import GrpcHeaderNames.GRPC_STATUS;
import GrpcHeaderNames.GRPC_TIMEOUT;
import GrpcUnsafeBufferUtil.BUFFERS;
import RequestLogAvailability.COMPLETE;
import Status.DEADLINE_EXCEEDED;
import Status.OK;
import Status.UNIMPLEMENTED;
import Status.UNKNOWN;
import StreamingOutputCallRequest.Builder;
import com.google.protobuf.ByteString;
import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.logging.LoggingClientBuilder;
import com.linecorp.armeria.common.FilteredHttpResponse;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpObject;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.RequestContext;
import com.linecorp.armeria.common.RpcRequest;
import com.linecorp.armeria.common.RpcResponse;
import com.linecorp.armeria.common.logging.RequestLog;
import com.linecorp.armeria.common.util.EventLoopGroups;
import com.linecorp.armeria.common.util.Exceptions;
import com.linecorp.armeria.grpc.testing.Messages.EchoStatus;
import com.linecorp.armeria.grpc.testing.Messages.Payload;
import com.linecorp.armeria.grpc.testing.Messages.ResponseParameters;
import com.linecorp.armeria.grpc.testing.Messages.SimpleRequest;
import com.linecorp.armeria.grpc.testing.Messages.SimpleResponse;
import com.linecorp.armeria.grpc.testing.Messages.StreamingInputCallRequest;
import com.linecorp.armeria.grpc.testing.Messages.StreamingInputCallResponse;
import com.linecorp.armeria.grpc.testing.Messages.StreamingOutputCallRequest;
import com.linecorp.armeria.grpc.testing.Messages.StreamingOutputCallResponse;
import com.linecorp.armeria.grpc.testing.TestServiceGrpc;
import com.linecorp.armeria.grpc.testing.TestServiceGrpc.TestServiceBlockingStub;
import com.linecorp.armeria.grpc.testing.TestServiceGrpc.TestServiceStub;
import com.linecorp.armeria.grpc.testing.UnimplementedServiceGrpc;
import com.linecorp.armeria.internal.grpc.StreamRecorder;
import com.linecorp.armeria.internal.grpc.TestServiceImpl;
import com.linecorp.armeria.internal.grpc.TimeoutHeaderUtil;
import com.linecorp.armeria.protobuf.EmptyProtos.Empty;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.grpc.GrpcServiceBuilder;
import com.linecorp.armeria.testing.server.ServerRule;
import com.linecorp.armeria.unsafe.grpc.GrpcUnsafeBufferUtil;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;


public class GrpcClientTest {
    /**
     * Must be at least {@link #unaryPayloadLength()}, plus some to account for encoding overhead.
     */
    private static final int MAX_MESSAGE_SIZE = (16 * 1024) * 1024;

    private static final Empty EMPTY = Empty.getDefaultInstance();

    private static final AtomicReference<HttpHeaders> CLIENT_HEADERS_CAPTURE = new AtomicReference<>();

    private static final AtomicReference<HttpHeaders> SERVER_TRAILERS_CAPTURE = new AtomicReference<>();

    @ClassRule
    public static ServerRule server = new ServerRule() {
        @Override
        protected void configure(ServerBuilder sb) throws Exception {
            sb.workerGroup(EventLoopGroups.newEventLoopGroup(1), true);
            sb.defaultMaxRequestLength(GrpcClientTest.MAX_MESSAGE_SIZE);
            sb.idleTimeoutMillis(0);
            sb.serviceUnder("/", new GrpcServiceBuilder().addService(new TestServiceImpl(Executors.newSingleThreadScheduledExecutor())).setMaxInboundMessageSizeBytes(GrpcClientTest.MAX_MESSAGE_SIZE).setMaxOutboundMessageSizeBytes(GrpcClientTest.MAX_MESSAGE_SIZE).build().decorate(TestServiceImpl.EchoRequestHeadersInTrailers::new).decorate(( client, ctx, req) -> {
                GrpcClientTest.CLIENT_HEADERS_CAPTURE.set(req.headers());
                final HttpResponse res = client.serve(ctx, req);
                return new FilteredHttpResponse(res) {
                    private boolean headersReceived;

                    @Override
                    protected HttpObject filter(HttpObject obj) {
                        if (obj instanceof HttpHeaders) {
                            if (!(headersReceived)) {
                                headersReceived = true;
                            } else {
                                GrpcClientTest.SERVER_TRAILERS_CAPTURE.set(((HttpHeaders) (obj)));
                            }
                        }
                        return obj;
                    }
                };
            }));
        }
    };

    @Rule
    public TestRule globalTimeout = new DisableOnDebug(new Timeout(10, TimeUnit.SECONDS));

    private final BlockingQueue<RequestLog> requestLogQueue = new LinkedTransferQueue<>();

    private TestServiceBlockingStub blockingStub;

    private TestServiceStub asyncStub;

    @Test
    public void emptyUnary() throws Exception {
        assertThat(blockingStub.emptyCall(GrpcClientTest.EMPTY)).isEqualTo(GrpcClientTest.EMPTY);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(GrpcClientTest.EMPTY);
            assertThat(rpcRes.get()).isEqualTo(GrpcClientTest.EMPTY);
        });
    }

    @Test
    public void largeUnary() throws Exception {
        final SimpleRequest request = SimpleRequest.newBuilder().setResponseSize(314159).setResponseType(COMPRESSABLE).setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[271828]))).build();
        final SimpleResponse goldenResponse = SimpleResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[314159]))).build();
        assertThat(blockingStub.unaryCall(request)).isEqualTo(goldenResponse);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(rpcRes.get()).isEqualTo(goldenResponse);
        });
    }

    @Test
    public void largeUnary_unsafe() throws Exception {
        final SimpleRequest request = SimpleRequest.newBuilder().setResponseSize(314159).setResponseType(COMPRESSABLE).setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[271828]))).build();
        final SimpleResponse goldenResponse = SimpleResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[314159]))).build();
        final TestServiceStub stub = new com.linecorp.armeria.client.ClientBuilder(("gproto+" + (GrpcClientTest.server.httpUri("/")))).option(UNSAFE_WRAP_RESPONSE_BUFFERS.newValue(true)).decorator(new LoggingClientBuilder().newDecorator()).build(TestServiceStub.class);
        final BlockingQueue<Object> resultQueue = new LinkedTransferQueue<>();
        stub.unaryCall(request, new io.grpc.stub.StreamObserver<SimpleResponse>() {
            @Override
            public void onNext(SimpleResponse value) {
                try {
                    final RequestContext ctx = RequestContext.current();
                    assertThat(value).isEqualTo(goldenResponse);
                    final ByteBuf buf = ctx.attr(BUFFERS).get().get(value);
                    assertThat(buf.refCnt()).isNotZero();
                    GrpcUnsafeBufferUtil.releaseBuffer(value, ctx);
                    assertThat(buf.refCnt()).isZero();
                } catch (Throwable t) {
                    resultQueue.add(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                resultQueue.add(t);
            }

            @Override
            public void onCompleted() {
                resultQueue.add("OK");
            }
        });
        final Object result = resultQueue.poll(10, TimeUnit.SECONDS);
        if (result instanceof Throwable) {
            Exceptions.throwUnsafely(((Throwable) (result)));
        } else {
            assertThat(result).isEqualTo("OK");
            assertThat(resultQueue.poll(1, TimeUnit.SECONDS)).isNull();
        }
    }

    @Test
    public void serverStreaming() throws Exception {
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().setResponseType(COMPRESSABLE).addResponseParameters(ResponseParameters.newBuilder().setSize(31415)).addResponseParameters(ResponseParameters.newBuilder().setSize(9)).addResponseParameters(ResponseParameters.newBuilder().setSize(2653)).addResponseParameters(ResponseParameters.newBuilder().setSize(58979)).build();
        final List<StreamingOutputCallResponse> goldenResponses = Arrays.asList(StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[31415]))).build(), StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[9]))).build(), StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[2653]))).build(), StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[58979]))).build());
        final StreamRecorder<StreamingOutputCallResponse> recorder = StreamRecorder.create();
        asyncStub.streamingOutputCall(request, recorder);
        recorder.awaitCompletion();
        GrpcClientTest.assertSuccess(recorder);
        assertThat(recorder.getValues()).containsExactlyElementsOf(goldenResponses);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(rpcRes.get()).isEqualTo(goldenResponses.get(0));
        });
    }

    @Test
    public void clientStreaming() throws Exception {
        final List<StreamingInputCallRequest> requests = Arrays.asList(StreamingInputCallRequest.newBuilder().setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[27182]))).build(), StreamingInputCallRequest.newBuilder().setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[8]))).build(), StreamingInputCallRequest.newBuilder().setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[1828]))).build(), StreamingInputCallRequest.newBuilder().setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[45904]))).build());
        final StreamingInputCallResponse goldenResponse = StreamingInputCallResponse.newBuilder().setAggregatedPayloadSize(74922).build();
        final StreamRecorder<StreamingInputCallResponse> responseObserver = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingInputCallRequest> requestObserver = asyncStub.streamingInputCall(responseObserver);
        for (StreamingInputCallRequest request : requests) {
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();
        assertThat(responseObserver.firstValue().get()).isEqualTo(goldenResponse);
        responseObserver.awaitCompletion();
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(requests.get(0));
            assertThat(rpcRes.get()).isEqualTo(goldenResponse);
        });
    }

    @Test
    public void pingPong() throws Exception {
        final List<StreamingOutputCallRequest> requests = Arrays.asList(StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(31415)).setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[27182]))).build(), StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(9)).setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[8]))).build(), StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(2653)).setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[1828]))).build(), StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(58979)).setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[45904]))).build());
        final List<StreamingOutputCallResponse> goldenResponses = Arrays.asList(StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[31415]))).build(), StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[9]))).build(), StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[2653]))).build(), StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[58979]))).build());
        final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(5);
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestObserver = asyncStub.fullDuplexCall(new io.grpc.stub.StreamObserver<StreamingOutputCallResponse>() {
            @Override
            public void onNext(StreamingOutputCallResponse response) {
                queue.add(response);
            }

            @Override
            public void onError(Throwable t) {
                queue.add(t);
            }

            @Override
            public void onCompleted() {
                queue.add("Completed");
            }
        });
        for (int i = 0; i < (requests.size()); i++) {
            assertThat(queue.peek()).isNull();
            requestObserver.onNext(requests.get(i));
            assertThat(queue.poll(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS)).isEqualTo(goldenResponses.get(i));
        }
        requestObserver.onCompleted();
        assertThat(queue.poll(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS)).isEqualTo("Completed");
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(requests.get(0));
            assertThat(rpcRes.get()).isEqualTo(goldenResponses.get(0));
        });
    }

    @Test
    public void emptyStream() throws Exception {
        final StreamRecorder<StreamingOutputCallResponse> responseObserver = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestObserver = asyncStub.fullDuplexCall(responseObserver);
        requestObserver.onCompleted();
        responseObserver.awaitCompletion(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).isEmpty();
            assertThat(rpcRes.get()).isNull();
        });
    }

    @Test
    public void cancelAfterBegin() throws Exception {
        final StreamRecorder<StreamingInputCallResponse> responseObserver = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingInputCallRequest> requestObserver = asyncStub.streamingInputCall(responseObserver);
        requestObserver.onError(new RuntimeException());
        responseObserver.awaitCompletion();
        assertThat(responseObserver.getValues()).isEmpty();
        assertThat(Status.fromThrowable(responseObserver.getError()).getCode()).isEqualTo(CANCELLED);
        final RequestLog log = requestLogQueue.take();
        assertThat(log.availabilities()).contains(COMPLETE);
        assertThat(log.responseContent()).isInstanceOf(RpcResponse.class);
        final Throwable cause = cause();
        assertThat(cause).isInstanceOf(StatusException.class);
        assertThat(getStatus().getCode()).isEqualTo(CANCELLED);
    }

    @Test
    public void cancelAfterFirstResponse() throws Exception {
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(31415)).setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[27182]))).build();
        final StreamingOutputCallResponse goldenResponse = StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[31415]))).build();
        final StreamRecorder<StreamingOutputCallResponse> responseObserver = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestObserver = asyncStub.fullDuplexCall(responseObserver);
        requestObserver.onNext(request);
        await().untilAsserted(() -> assertThat(responseObserver.firstValue().get()).isEqualTo(goldenResponse));
        requestObserver.onError(new RuntimeException());
        responseObserver.awaitCompletion(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS);
        assertThat(responseObserver.getValues()).hasSize(1);
        assertThat(Status.fromThrowable(responseObserver.getError()).getCode()).isEqualTo(CANCELLED);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(grpcStatus.getCode()).isEqualTo(CANCELLED);
        });
    }

    @Test
    public void fullDuplexCallShouldSucceed() throws Exception {
        // Build the request.
        final List<Integer> responseSizes = Arrays.asList(50, 100, 150, 200);
        final StreamingOutputCallRequest.Builder streamingOutputBuilder = StreamingOutputCallRequest.newBuilder();
        streamingOutputBuilder.setResponseType(COMPRESSABLE);
        for (Integer size : responseSizes) {
            streamingOutputBuilder.addResponseParametersBuilder().setSize(size).setIntervalUs(0);
        }
        final StreamingOutputCallRequest request = streamingOutputBuilder.build();
        final StreamRecorder<StreamingOutputCallResponse> recorder = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestStream = asyncStub.fullDuplexCall(recorder);
        final int numRequests = 10;
        final List<StreamingOutputCallRequest> requests = new ArrayList<>(numRequests);
        for (int ix = numRequests; ix > 0; --ix) {
            requests.add(request);
            requestStream.onNext(request);
        }
        requestStream.onCompleted();
        recorder.awaitCompletion();
        GrpcClientTest.assertSuccess(recorder);
        assertThat(recorder.getValues()).hasSize(((responseSizes.size()) * numRequests));
        for (int ix = 0; ix < (recorder.getValues().size()); ++ix) {
            final StreamingOutputCallResponse response = recorder.getValues().get(ix);
            assertThat(response.getPayload().getType()).isEqualTo(COMPRESSABLE);
            final int length = response.getPayload().getBody().size();
            final int expectedSize = responseSizes.get((ix % (responseSizes.size())));
            assertThat(length).withFailMessage(("comparison failed at index " + ix)).isEqualTo(expectedSize);
        }
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(requests.get(0));
            assertThat(rpcRes.get()).isEqualTo(recorder.getValues().get(0));
        });
    }

    @Test
    public void halfDuplexCallShouldSucceed() throws Exception {
        // Build the request.
        final List<Integer> responseSizes = Arrays.asList(50, 100, 150, 200);
        final StreamingOutputCallRequest.Builder streamingOutputBuilder = StreamingOutputCallRequest.newBuilder();
        streamingOutputBuilder.setResponseType(COMPRESSABLE);
        for (Integer size : responseSizes) {
            streamingOutputBuilder.addResponseParametersBuilder().setSize(size).setIntervalUs(0);
        }
        final StreamingOutputCallRequest request = streamingOutputBuilder.build();
        final StreamRecorder<StreamingOutputCallResponse> recorder = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestStream = asyncStub.halfDuplexCall(recorder);
        final int numRequests = 10;
        final List<StreamingOutputCallRequest> requests = new ArrayList<>(numRequests);
        for (int ix = numRequests; ix > 0; --ix) {
            requests.add(request);
            requestStream.onNext(request);
        }
        requestStream.onCompleted();
        recorder.awaitCompletion();
        GrpcClientTest.assertSuccess(recorder);
        assertThat(recorder.getValues()).hasSize(((responseSizes.size()) * numRequests));
        for (int ix = 0; ix < (recorder.getValues().size()); ++ix) {
            final StreamingOutputCallResponse response = recorder.getValues().get(ix);
            assertThat(response.getPayload().getType()).isEqualTo(COMPRESSABLE);
            final int length = response.getPayload().getBody().size();
            final int expectedSize = responseSizes.get((ix % (responseSizes.size())));
            assertThat(length).withFailMessage(("comparison failed at index " + ix)).isEqualTo(expectedSize);
        }
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(requests.get(0));
            assertThat(rpcRes.get()).isEqualTo(recorder.getValues().get(0));
        });
    }

    @Test
    public void serverStreamingShouldBeFlowControlled() throws Exception {
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().setResponseType(COMPRESSABLE).addResponseParameters(ResponseParameters.newBuilder().setSize(100000)).addResponseParameters(ResponseParameters.newBuilder().setSize(100001)).build();
        final List<StreamingOutputCallResponse> goldenResponses = Arrays.asList(StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[100000]))).build(), StreamingOutputCallResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[100001]))).build());
        final long start = System.nanoTime();
        final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(10);
        final ClientCall<StreamingOutputCallRequest, StreamingOutputCallResponse> call = asyncStub.getChannel().newCall(TestServiceGrpc.getStreamingOutputCallMethod(), DEFAULT);
        call.start(new ClientCall.Listener<StreamingOutputCallResponse>() {
            @Override
            public void onHeaders(Metadata headers) {
            }

            @Override
            public void onMessage(final StreamingOutputCallResponse message) {
                queue.add(message);
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                queue.add(status);
            }
        }, new Metadata());
        call.sendMessage(request);
        call.halfClose();
        // Time how long it takes to get the first response.
        call.request(1);
        final Object actualResponse1 = queue.poll(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS);
        assertThat(actualResponse1).withFailMessage("Unexpected response: %s", actualResponse1).isEqualTo(goldenResponses.get(0));
        final long firstCallDuration = (System.nanoTime()) - start;
        // Without giving additional flow control, make sure that we don't get another response. We wait
        // until we are comfortable the next message isn't coming. We may have very low nanoTime
        // resolution (like on Windows) or be using a testing, in-process transport where message
        // handling is instantaneous. In both cases, firstCallDuration may be 0, so round up sleep time
        // to at least 1ms.
        assertThat(queue.poll(Math.max((firstCallDuration * 4), 1000000), TimeUnit.NANOSECONDS)).isNull();
        // Make sure that everything still completes.
        call.request(1);
        final Object actualResponse2 = queue.poll(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS);
        assertThat(actualResponse2).withFailMessage("Unexpected response: %s", actualResponse2).isEqualTo(goldenResponses.get(1));
        assertThat(queue.poll(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS)).isEqualTo(OK);
        call.cancel("Cancelled after all of the requests are done", null);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(rpcRes.get()).isEqualTo(goldenResponses.get(0));
        });
    }

    @Test
    public void veryLargeRequest() throws Exception {
        final SimpleRequest request = SimpleRequest.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[GrpcClientTest.unaryPayloadLength()]))).setResponseSize(10).setResponseType(COMPRESSABLE).build();
        final SimpleResponse goldenResponse = SimpleResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[10]))).build();
        assertThat(blockingStub.unaryCall(request)).isEqualTo(goldenResponse);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(rpcRes.get()).isEqualTo(goldenResponse);
        });
    }

    @Test
    public void veryLargeResponse() throws Exception {
        final SimpleRequest request = SimpleRequest.newBuilder().setResponseSize(GrpcClientTest.unaryPayloadLength()).setResponseType(COMPRESSABLE).build();
        final SimpleResponse goldenResponse = SimpleResponse.newBuilder().setPayload(Payload.newBuilder().setType(COMPRESSABLE).setBody(ByteString.copyFrom(new byte[GrpcClientTest.unaryPayloadLength()]))).build();
        assertThat(blockingStub.unaryCall(request)).isEqualTo(goldenResponse);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(rpcRes.get()).isEqualTo(goldenResponse);
        });
    }

    @Test
    public void exchangeHeadersUnaryCall() throws Exception {
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, HTTP_HEADERS.newValue(HttpHeaders.of().set(TestServiceImpl.EXTRA_HEADER_NAME, "dog")));
        assertThat(stub.emptyCall(GrpcClientTest.EMPTY)).isNotNull();
        // Assert that our side channel object is echoed back in both headers and trailers
        assertThat(GrpcClientTest.CLIENT_HEADERS_CAPTURE.get().get(TestServiceImpl.EXTRA_HEADER_NAME)).isEqualTo("dog");
        assertThat(GrpcClientTest.SERVER_TRAILERS_CAPTURE.get().get(TestServiceImpl.EXTRA_HEADER_NAME)).isEqualTo("dog");
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(GrpcClientTest.EMPTY);
            assertThat(rpcRes.get()).isEqualTo(GrpcClientTest.EMPTY);
        });
    }

    @Test
    public void exchangeHeadersStreamingCall() throws Exception {
        final TestServiceStub stub = Clients.newDerivedClient(asyncStub, HTTP_HEADERS.newValue(HttpHeaders.of().set(TestServiceImpl.EXTRA_HEADER_NAME, "dog")));
        final List<Integer> responseSizes = Arrays.asList(50, 100, 150, 200);
        final StreamingOutputCallRequest.Builder streamingOutputBuilder = StreamingOutputCallRequest.newBuilder();
        streamingOutputBuilder.setResponseType(COMPRESSABLE);
        for (Integer size : responseSizes) {
            streamingOutputBuilder.addResponseParametersBuilder().setSize(size).setIntervalUs(0);
        }
        final StreamingOutputCallRequest request = streamingOutputBuilder.build();
        final StreamRecorder<StreamingOutputCallResponse> recorder = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestStream = stub.fullDuplexCall(recorder);
        final int numRequests = 10;
        final List<StreamingOutputCallRequest> requests = new ArrayList<>(numRequests);
        for (int ix = numRequests; ix > 0; --ix) {
            requests.add(request);
            requestStream.onNext(request);
        }
        requestStream.onCompleted();
        recorder.awaitCompletion();
        GrpcClientTest.assertSuccess(recorder);
        assertThat(recorder.getValues()).hasSize(((responseSizes.size()) * numRequests));
        // Assert that our side channel object is echoed back in both headers and trailers
        assertThat(GrpcClientTest.CLIENT_HEADERS_CAPTURE.get().get(TestServiceImpl.EXTRA_HEADER_NAME)).isEqualTo("dog");
        assertThat(GrpcClientTest.SERVER_TRAILERS_CAPTURE.get().get(TestServiceImpl.EXTRA_HEADER_NAME)).isEqualTo("dog");
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(requests.get(0));
            assertThat(rpcRes.get()).isEqualTo(recorder.getValues().get(0));
        });
    }

    @Test
    public void sendsTimeoutHeader() {
        final long configuredTimeoutMinutes = 100;
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, DEFAULT_RESPONSE_TIMEOUT_MILLIS.newValue(TimeUnit.MINUTES.toMillis(configuredTimeoutMinutes)));
        stub.emptyCall(GrpcClientTest.EMPTY);
        final long transferredTimeoutMinutes = TimeUnit.NANOSECONDS.toMinutes(TimeoutHeaderUtil.fromHeaderValue(GrpcClientTest.CLIENT_HEADERS_CAPTURE.get().get(GRPC_TIMEOUT)));
        assertThat(transferredTimeoutMinutes).isEqualTo(configuredTimeoutMinutes);
    }

    @Test
    public void deadlineNotExceeded() {
        // warm up the channel and JVM
        blockingStub.emptyCall(Empty.getDefaultInstance());
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, DEFAULT_RESPONSE_TIMEOUT_MILLIS.newValue(TimeUnit.SECONDS.toMillis(10)));
        stub.streamingOutputCall(StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setIntervalUs(0)).build()).next();
    }

    @Test
    public void deadlineExceeded() throws Exception {
        // warm up the channel and JVM
        blockingStub.emptyCall(Empty.getDefaultInstance());
        requestLogQueue.take();
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, DEFAULT_RESPONSE_TIMEOUT_MILLIS.newValue(10L));
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setIntervalUs(20000)).build();
        final Throwable t = catchThrowable(() -> stub.streamingOutputCall(request).next());
        assertThat(t).isInstanceOf(StatusRuntimeException.class);
        assertThat(getStatus().getCode()).isEqualTo(DEADLINE_EXCEEDED.getCode());
        checkRequestLogError(( headers, rpcReq, cause) -> {
            assertThat(rpcReq).isNotNull();
            assertThat(rpcReq.params()).containsExactly(request);
            GrpcClientTest.assertResponseTimeoutExceptionOrDeadlineExceeded(cause);
        });
    }

    @Test
    public void deadlineExceededServerStreaming() throws Exception {
        // warm up the channel and JVM
        blockingStub.emptyCall(Empty.getDefaultInstance());
        requestLogQueue.take();
        final ResponseParameters.Builder responseParameters = ResponseParameters.newBuilder().setSize(1).setIntervalUs(20000);
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().setResponseType(COMPRESSABLE).addResponseParameters(responseParameters).addResponseParameters(responseParameters).addResponseParameters(responseParameters).addResponseParameters(responseParameters).build();
        final StreamRecorder<StreamingOutputCallResponse> recorder = StreamRecorder.create();
        final TestServiceStub stub = Clients.newDerivedClient(asyncStub, DEFAULT_RESPONSE_TIMEOUT_MILLIS.newValue(30L));
        stub.streamingOutputCall(request, recorder);
        recorder.awaitCompletion();
        assertThat(recorder.getError()).isNotNull();
        assertThat(Status.fromThrowable(recorder.getError()).getCode()).isEqualTo(DEADLINE_EXCEEDED.getCode());
        checkRequestLogError(( headers, rpcReq, cause) -> {
            assertThat(rpcReq).isNotNull();
            assertThat(rpcReq.params()).containsExactly(request);
            GrpcClientTest.assertResponseTimeoutExceptionOrDeadlineExceeded(cause);
        });
    }

    @Test
    public void maxInboundSize_exact() {
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(1)).build();
        final int size = blockingStub.streamingOutputCall(request).next().getSerializedSize();
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, MAX_INBOUND_MESSAGE_SIZE_BYTES.newValue(size));
        stub.streamingOutputCall(request).next();
    }

    @Test
    public void maxInboundSize_tooBig() throws Exception {
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(1)).build();
        final int size = blockingStub.streamingOutputCall(request).next().getSerializedSize();
        requestLogQueue.take();
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, MAX_INBOUND_MESSAGE_SIZE_BYTES.newValue((size - 1)));
        final Throwable t = catchThrowable(() -> stub.streamingOutputCall(request).next());
        assertThat(t).isInstanceOf(StatusRuntimeException.class);
        assertThat(getStatus().getCode()).isEqualTo(RESOURCE_EXHAUSTED);
        assertThat(Exceptions.traceText(t)).contains("exceeds maximum");
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(grpcStatus).isNotNull();
            assertThat(grpcStatus.getCode()).isEqualTo(RESOURCE_EXHAUSTED);
        });
    }

    @Test
    public void maxOutboundSize_exact() {
        // set at least one field to ensure the size is non-zero.
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(1)).build();
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, MAX_OUTBOUND_MESSAGE_SIZE_BYTES.newValue(request.getSerializedSize()));
        stub.streamingOutputCall(request).next();
    }

    @Test
    public void maxOutboundSize_tooBig() throws Exception {
        // set at least one field to ensure the size is non-zero.
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().addResponseParameters(ResponseParameters.newBuilder().setSize(1)).build();
        final TestServiceBlockingStub stub = Clients.newDerivedClient(blockingStub, MAX_OUTBOUND_MESSAGE_SIZE_BYTES.newValue(((request.getSerializedSize()) - 1)));
        final Throwable t = catchThrowable(() -> stub.streamingOutputCall(request).next());
        assertThat(t).isInstanceOf(StatusRuntimeException.class);
        assertThat(getStatus().getCode()).isEqualTo(CANCELLED);
        assertThat(Exceptions.traceText(t)).contains("message too large");
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(request);
            assertThat(grpcStatus).isNotNull();
            assertThat(grpcStatus.getCode()).isEqualTo(CANCELLED);
        });
    }

    @Test
    public void statusCodeAndMessage() throws Exception {
        final int errorCode = 2;
        final String errorMessage = "test status message";
        final EchoStatus responseStatus = EchoStatus.newBuilder().setCode(errorCode).setMessage(errorMessage).build();
        final SimpleRequest simpleRequest = SimpleRequest.newBuilder().setResponseStatus(responseStatus).build();
        final StreamingOutputCallRequest streamingRequest = StreamingOutputCallRequest.newBuilder().setResponseStatus(responseStatus).build();
        // Test UnaryCall
        final Throwable t = catchThrowable(() -> blockingStub.unaryCall(simpleRequest));
        assertThat(t).isInstanceOf(StatusRuntimeException.class);
        final StatusRuntimeException e = ((StatusRuntimeException) (t));
        assertThat(e.getStatus().getCode()).isEqualTo(UNKNOWN.getCode());
        assertThat(e.getStatus().getDescription()).isEqualTo(errorMessage);
        // Test FullDuplexCall
        @SuppressWarnings("unchecked")
        final io.grpc.stub.StreamObserver<StreamingOutputCallResponse> responseObserver = Mockito.mock(io.grpc.stub.StreamObserver.class);
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestObserver = asyncStub.fullDuplexCall(responseObserver);
        requestObserver.onNext(streamingRequest);
        requestObserver.onCompleted();
        final ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        Mockito.verify(responseObserver, Mockito.timeout(GrpcClientTest.operationTimeoutMillis())).onError(captor.capture());
        assertThat(Status.fromThrowable(captor.getValue()).getCode()).isEqualTo(UNKNOWN.getCode());
        assertThat(Status.fromThrowable(captor.getValue()).getDescription()).isEqualTo(errorMessage);
        Mockito.verifyNoMoreInteractions(responseObserver);
        checkRequestLog(( rpcReq, rpcRes, grpcStatus) -> {
            assertThat(rpcReq.params()).containsExactly(simpleRequest);
            assertThat(grpcStatus).isNotNull();
            assertThat(grpcStatus.getCode()).isEqualTo(Code.UNKNOWN);
            assertThat(grpcStatus.getDescription()).isEqualTo(errorMessage);
        });
    }

    /**
     * Sends an rpc to an unimplemented method within TestService.
     */
    @Test
    public void unimplementedMethod() throws Exception {
        final Throwable t = catchThrowable(() -> blockingStub.unimplementedCall(Empty.getDefaultInstance()));
        assertThat(t).isInstanceOf(StatusRuntimeException.class);
        assertThat(getStatus().getCode()).isEqualTo(UNIMPLEMENTED.getCode());
        checkRequestLogError(( headers, rpcReq, cause) -> {
            assertThat(rpcReq).isNotNull();
            assertThat(rpcReq.params()).containsExactly(Empty.getDefaultInstance());
            assertThat(headers.get(GRPC_STATUS)).isEqualTo(String.valueOf(UNIMPLEMENTED.getCode().value()));
        });
    }

    @Test
    public void unimplementedService() throws Exception {
        final UnimplementedServiceGrpc.UnimplementedServiceBlockingStub stub = UnimplementedServiceGrpc.newBlockingStub(asyncStub.getChannel());
        final Throwable t = catchThrowable(() -> stub.unimplementedCall(Empty.getDefaultInstance()));
        assertThat(t).isInstanceOf(StatusRuntimeException.class);
        assertThat(getStatus().getCode()).isEqualTo(UNIMPLEMENTED.getCode());
        checkRequestLogError(( headers, rpcReq, cause) -> {
            // rpcReq may be null depending on timing.
            if (rpcReq != null) {
                assertThat(rpcReq.params()).containsExactly(Empty.getDefaultInstance());
            }
            assertThat(headers.get(GRPC_STATUS)).isEqualTo(String.valueOf(UNIMPLEMENTED.getCode().value()));
        });
    }

    /**
     * Start a fullDuplexCall which the server will not respond, and verify the deadline expires.
     */
    @Test
    public void timeoutOnSleepingServer() throws Exception {
        final TestServiceStub stub = Clients.newDerivedClient(asyncStub, DEFAULT_RESPONSE_TIMEOUT_MILLIS.newValue(1L));
        final StreamRecorder<StreamingOutputCallResponse> responseObserver = StreamRecorder.create();
        final io.grpc.stub.StreamObserver<StreamingOutputCallRequest> requestObserver = stub.fullDuplexCall(responseObserver);
        final StreamingOutputCallRequest request = StreamingOutputCallRequest.newBuilder().setPayload(Payload.newBuilder().setBody(ByteString.copyFrom(new byte[27182]))).addResponseParameters(ResponseParameters.newBuilder().setIntervalUs(((int) (TimeUnit.SECONDS.toMicros(10))))).build();
        try {
            requestObserver.onNext(request);
            // TODO(anuraag): Upstream test does not need to call onCompleted - response timeout is set at the
            // start of the stream, which seems to make sense. Figure out how to fix this timeout handling in
            // armeria.
            requestObserver.onCompleted();
        } catch (IllegalStateException expected) {
            // This can happen if the stream has already been terminated due to deadline exceeded.
        }
        responseObserver.awaitCompletion(GrpcClientTest.operationTimeoutMillis(), TimeUnit.MILLISECONDS);
        assertThat(responseObserver.getValues()).isEmpty();
        assertThat(responseObserver.getError()).isNotNull();
        // TODO(anuraag): As gRPC supports handling timeouts in the server or client due to the grpc-timeout
        // header, it's not guaranteed which is the source of this error.
        // Until https://github.com/line/armeria/issues/521 a server side timeout will not have the correct
        // status so we don't verify it for now.
        // assertThat(Status.fromThrowable(responseObserver.getError()).getCode())
        // .isEqualTo(Status.DEADLINE_EXCEEDED.getCode());
    }

    @Test
    public void nonAsciiStatusMessage() {
        final String statusMessage = "????";
        assertThatThrownBy(() -> blockingStub.unaryCall(SimpleRequest.newBuilder().setResponseStatus(EchoStatus.newBuilder().setCode(2).setMessage(statusMessage)).build())).isInstanceOfSatisfying(StatusRuntimeException.class, ( e) -> assertThat(e.getStatus().getDescription()).isEqualTo(statusMessage));
    }

    @FunctionalInterface
    private interface RequestLogChecker {
        void check(RpcRequest rpcReq, RpcResponse rpcRes, @Nullable
        Status grpcStatus) throws Exception;
    }

    @FunctionalInterface
    private interface RequestLogErrorChecker {
        void check(HttpHeaders headers, @Nullable
        RpcRequest rpcReq, @Nullable
        Throwable cause) throws Exception;
    }
}
