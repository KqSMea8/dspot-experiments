/**
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2;


@java.lang.SuppressWarnings("unchecked")
public final class AmplExecutorCallAdapterFactoryTest {
    static class EmptyCall implements retrofit2.Call<java.lang.String> {
        @java.lang.Override
        public void enqueue(retrofit2.Callback<java.lang.String> callback) {
            throw new java.lang.UnsupportedOperationException();
        }

        @java.lang.Override
        public boolean isExecuted() {
            return false;
        }

        @java.lang.Override
        public retrofit2.Response<java.lang.String> execute() throws java.io.IOException {
            throw new java.lang.UnsupportedOperationException();
        }

        @java.lang.Override
        public void cancel() {
            throw new java.lang.UnsupportedOperationException();
        }

        @java.lang.Override
        public boolean isCanceled() {
            return false;
        }

        @java.lang.Override
        public retrofit2.Call<java.lang.String> clone() {
            throw new java.lang.UnsupportedOperationException();
        }

        @java.lang.Override
        public okhttp3.Request request() {
            throw new java.lang.UnsupportedOperationException();
        }
    }

    private static final java.lang.annotation.Annotation[] NO_ANNOTATIONS = new java.lang.annotation.Annotation[0];

    private final retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder().baseUrl("http://localhost:1").build();

    private final retrofit2.Callback<java.lang.String> callback = org.mockito.Mockito.mock(retrofit2.Callback.class);

    private final java.util.concurrent.Executor callbackExecutor = org.mockito.Mockito.spy(new java.util.concurrent.Executor() {
        @java.lang.Override
        public void execute(java.lang.Runnable runnable) {
            runnable.run();
        }
    });

    private final retrofit2.CallAdapter.Factory factory = new retrofit2.ExecutorCallAdapterFactory(callbackExecutor);

    @org.junit.Test
    public void rawTypeThrows() {
        try {
            factory.get(retrofit2.Call.class, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit);
            org.junit.Assert.fail();
        } catch (java.lang.IllegalArgumentException e) {
            org.assertj.core.api.Assertions.assertThat(e).hasMessage("Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        }
    }

    @org.junit.Test
    public void responseType() {
        java.lang.reflect.Type classType = new com.google.common.reflect.TypeToken<retrofit2.Call<java.lang.String>>() {}.getType();
        org.assertj.core.api.Assertions.assertThat(factory.get(classType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit).responseType()).isEqualTo(java.lang.String.class);
        java.lang.reflect.Type wilcardType = new com.google.common.reflect.TypeToken<retrofit2.Call<? extends java.lang.String>>() {}.getType();
        org.assertj.core.api.Assertions.assertThat(factory.get(wilcardType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit).responseType()).isEqualTo(java.lang.String.class);
        java.lang.reflect.Type genericType = new com.google.common.reflect.TypeToken<retrofit2.Call<java.util.List<java.lang.String>>>() {}.getType();
        org.assertj.core.api.Assertions.assertThat(factory.get(genericType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit).responseType()).isEqualTo(new com.google.common.reflect.TypeToken<java.util.List<java.lang.String>>() {}.getType());
    }

    @org.junit.Test
    public void adaptedCallExecute() throws java.io.IOException {
        java.lang.reflect.Type returnType = new com.google.common.reflect.TypeToken<retrofit2.Call<java.lang.String>>() {}.getType();
        retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>> adapter = ((retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>>) (factory.get(returnType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit)));
        final retrofit2.Response<java.lang.String> response = retrofit2.Response.success("Hi");
        retrofit2.Call<java.lang.String> call = adapter.adapt(new retrofit2.AmplExecutorCallAdapterFactoryTest.EmptyCall() {
            @java.lang.Override
            public retrofit2.Response<java.lang.String> execute() throws java.io.IOException {
                return response;
            }
        });
        org.assertj.core.api.Assertions.assertThat(call.execute()).isSameAs(response);
    }

    @org.junit.Test
    public void adaptedCallEnqueueUsesExecutorForSuccessCallback() {
        java.lang.reflect.Type returnType = new com.google.common.reflect.TypeToken<retrofit2.Call<java.lang.String>>() {}.getType();
        retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>> adapter = ((retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>>) (factory.get(returnType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit)));
        final retrofit2.Response<java.lang.String> response = retrofit2.Response.success("Hi");
        retrofit2.AmplExecutorCallAdapterFactoryTest.EmptyCall originalCall = new retrofit2.AmplExecutorCallAdapterFactoryTest.EmptyCall() {
            @java.lang.Override
            public void enqueue(retrofit2.Callback<java.lang.String> callback) {
                callback.onResponse(this, response);
            }
        };
        retrofit2.Call<java.lang.String> call = adapter.adapt(originalCall);
        call.enqueue(callback);
        org.mockito.Mockito.verify(callbackExecutor).execute(org.mockito.Matchers.any(java.lang.Runnable.class));
        org.mockito.Mockito.verify(callback).onResponse(call, response);
    }

    @org.junit.Test
    public void adaptedCallEnqueueUsesExecutorForFailureCallback() {
        java.lang.reflect.Type returnType = new com.google.common.reflect.TypeToken<retrofit2.Call<java.lang.String>>() {}.getType();
        retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>> adapter = ((retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>>) (factory.get(returnType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit)));
        final java.lang.Throwable throwable = new java.io.IOException();
        retrofit2.AmplExecutorCallAdapterFactoryTest.EmptyCall originalCall = new retrofit2.AmplExecutorCallAdapterFactoryTest.EmptyCall() {
            @java.lang.Override
            public void enqueue(retrofit2.Callback<java.lang.String> callback) {
                callback.onFailure(this, throwable);
            }
        };
        retrofit2.Call<java.lang.String> call = adapter.adapt(originalCall);
        call.enqueue(callback);
        org.mockito.Mockito.verify(callbackExecutor).execute(org.mockito.Matchers.any(java.lang.Runnable.class));
        org.mockito.Mockito.verifyNoMoreInteractions(callbackExecutor);
        org.mockito.Mockito.verify(callback).onFailure(call, throwable);
        org.mockito.Mockito.verifyNoMoreInteractions(callback);
    }

    @org.junit.Test
    public void adaptedCallCloneDeepCopy() {
        java.lang.reflect.Type returnType = new com.google.common.reflect.TypeToken<retrofit2.Call<java.lang.String>>() {}.getType();
        retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>> adapter = ((retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>>) (factory.get(returnType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit)));
        retrofit2.Call<java.lang.String> delegate = org.mockito.Mockito.mock(retrofit2.Call.class);
        retrofit2.Call<java.lang.String> call = adapter.adapt(delegate);
        retrofit2.Call<java.lang.String> cloned = call.clone();
        org.assertj.core.api.Assertions.assertThat(cloned).isNotSameAs(call);
        org.mockito.Mockito.verify(delegate).clone();
        org.mockito.Mockito.verifyNoMoreInteractions(delegate);
    }

    @org.junit.Test
    public void adaptedCallCancel() {
        java.lang.reflect.Type returnType = new com.google.common.reflect.TypeToken<retrofit2.Call<java.lang.String>>() {}.getType();
        retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>> adapter = ((retrofit2.CallAdapter<java.lang.String, retrofit2.Call<java.lang.String>>) (factory.get(returnType, retrofit2.AmplExecutorCallAdapterFactoryTest.NO_ANNOTATIONS, retrofit)));
        retrofit2.Call<java.lang.String> delegate = org.mockito.Mockito.mock(retrofit2.Call.class);
        retrofit2.Call<java.lang.String> call = adapter.adapt(delegate);
        call.cancel();
        org.mockito.Mockito.verify(delegate).cancel();
        org.mockito.Mockito.verifyNoMoreInteractions(delegate);
    }
}

