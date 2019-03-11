/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.kstream.internals;


import java.util.Iterator;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsSame;
import org.junit.Test;


@SuppressWarnings("unchecked")
public class TransformerSupplierAdapterTest extends EasyMockSupport {
    private ProcessorContext context;

    private Transformer<String, String, KeyValue<Integer, Integer>> transformer;

    private TransformerSupplier<String, String, KeyValue<Integer, Integer>> transformerSupplier;

    final String key = "Hello";

    final String value = "World";

    @Test
    public void shouldCallInitOfAdapteeTransformer() {
        EasyMock.expect(transformerSupplier.get()).andReturn(transformer);
        transformer.init(context);
        replayAll();
        final TransformerSupplierAdapter<String, String, Integer, Integer> adapter = new TransformerSupplierAdapter(transformerSupplier);
        final Transformer<String, String, Iterable<KeyValue<Integer, Integer>>> adaptedTransformer = adapter.get();
        adaptedTransformer.init(context);
        verifyAll();
    }

    @Test
    public void shouldCallCloseOfAdapteeTransformer() {
        EasyMock.expect(transformerSupplier.get()).andReturn(transformer);
        transformer.close();
        replayAll();
        final TransformerSupplierAdapter<String, String, Integer, Integer> adapter = new TransformerSupplierAdapter(transformerSupplier);
        final Transformer<String, String, Iterable<KeyValue<Integer, Integer>>> adaptedTransformer = adapter.get();
        adaptedTransformer.close();
        verifyAll();
    }

    @Test
    public void shouldCallTransformOfAdapteeTransformerAndReturnSingletonIterable() {
        EasyMock.expect(transformerSupplier.get()).andReturn(transformer);
        EasyMock.expect(transformer.transform(key, value)).andReturn(KeyValue.pair(0, 1));
        replayAll();
        final TransformerSupplierAdapter<String, String, Integer, Integer> adapter = new TransformerSupplierAdapter(transformerSupplier);
        final Transformer<String, String, Iterable<KeyValue<Integer, Integer>>> adaptedTransformer = adapter.get();
        final Iterator<KeyValue<Integer, Integer>> iterator = adaptedTransformer.transform(key, value).iterator();
        verifyAll();
        MatcherAssert.assertThat(iterator.hasNext(), IsEqual.equalTo(true));
        iterator.next();
        MatcherAssert.assertThat(iterator.hasNext(), IsEqual.equalTo(false));
    }

    @Test
    public void shouldCallTransformOfAdapteeTransformerAndReturnEmptyIterable() {
        EasyMock.expect(transformerSupplier.get()).andReturn(transformer);
        EasyMock.expect(transformer.transform(key, value)).andReturn(null);
        replayAll();
        final TransformerSupplierAdapter<String, String, Integer, Integer> adapter = new TransformerSupplierAdapter(transformerSupplier);
        final Transformer<String, String, Iterable<KeyValue<Integer, Integer>>> adaptedTransformer = adapter.get();
        final Iterator<KeyValue<Integer, Integer>> iterator = adaptedTransformer.transform(key, value).iterator();
        verifyAll();
        MatcherAssert.assertThat(iterator.hasNext(), IsEqual.equalTo(false));
    }

    @Test
    public void shouldAlwaysGetNewAdapterTransformer() {
        final Transformer<String, String, KeyValue<Integer, Integer>> transformer1 = mock(Transformer.class);
        final Transformer<String, String, KeyValue<Integer, Integer>> transformer2 = mock(Transformer.class);
        final Transformer<String, String, KeyValue<Integer, Integer>> transformer3 = mock(Transformer.class);
        EasyMock.expect(transformerSupplier.get()).andReturn(transformer1);
        transformer1.init(context);
        EasyMock.expect(transformerSupplier.get()).andReturn(transformer2);
        transformer2.init(context);
        EasyMock.expect(transformerSupplier.get()).andReturn(transformer3);
        transformer3.init(context);
        replayAll();
        final TransformerSupplierAdapter<String, String, Integer, Integer> adapter = new TransformerSupplierAdapter(transformerSupplier);
        final Transformer<String, String, Iterable<KeyValue<Integer, Integer>>> adapterTransformer1 = adapter.get();
        adapterTransformer1.init(context);
        final Transformer<String, String, Iterable<KeyValue<Integer, Integer>>> adapterTransformer2 = adapter.get();
        adapterTransformer2.init(context);
        final Transformer<String, String, Iterable<KeyValue<Integer, Integer>>> adapterTransformer3 = adapter.get();
        adapterTransformer3.init(context);
        verifyAll();
        MatcherAssert.assertThat(adapterTransformer1, IsNot.not(IsSame.sameInstance(adapterTransformer2)));
        MatcherAssert.assertThat(adapterTransformer2, IsNot.not(IsSame.sameInstance(adapterTransformer3)));
        MatcherAssert.assertThat(adapterTransformer3, IsNot.not(IsSame.sameInstance(adapterTransformer1)));
    }
}
