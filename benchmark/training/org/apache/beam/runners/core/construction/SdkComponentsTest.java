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
package org.apache.beam.runners.core.construction;


import AccumulationMode.ACCUMULATING_FIRED_PANES;
import java.io.IOException;
import java.util.Collections;
import org.apache.beam.model.pipeline.v1.RunnerApi.Components;
import org.apache.beam.sdk.coders.ByteArrayCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.IterableCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SetCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarLongCoder;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.runners.AppliedPTransform;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.WindowingStrategy;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Tests for {@link SdkComponents}.
 */
@RunWith(JUnit4.class)
public class SdkComponentsTest {
    @Rule
    public TestPipeline pipeline = TestPipeline.create().enableAbandonedNodeEnforcement(false);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private SdkComponents components;

    @Test
    public void registerCoder() throws IOException {
        Coder<?> coder = KvCoder.of(StringUtf8Coder.of(), IterableCoder.of(SetCoder.of(ByteArrayCoder.of())));
        String id = components.registerCoder(coder);
        Assert.assertThat(components.registerCoder(coder), Matchers.equalTo(id));
        Assert.assertThat(id, Matchers.not(Matchers.isEmptyOrNullString()));
        Coder<?> equalCoder = KvCoder.of(StringUtf8Coder.of(), IterableCoder.of(SetCoder.of(ByteArrayCoder.of())));
        Assert.assertThat(components.registerCoder(equalCoder), Matchers.equalTo(id));
        Coder<?> otherCoder = VarLongCoder.of();
        Assert.assertThat(components.registerCoder(otherCoder), Matchers.not(Matchers.equalTo(id)));
        components.toComponents().getCodersOrThrow(id);
        components.toComponents().getCodersOrThrow(components.registerCoder(otherCoder));
    }

    @Test
    public void registerTransformNoChildren() throws IOException {
        Create.Values<Integer> create = Create.of(1, 2, 3);
        PCollection<Integer> pt = pipeline.apply(create);
        String userName = "my_transform/my_nesting";
        AppliedPTransform<?, ?, ?> transform = AppliedPTransform.of(userName, pipeline.begin().expand(), pt.expand(), create, pipeline);
        String componentName = components.registerPTransform(transform, Collections.emptyList());
        Assert.assertThat(componentName, Matchers.equalTo(userName));
        Assert.assertThat(components.getExistingPTransformId(transform), Matchers.equalTo(componentName));
    }

    @Test
    public void registerTransformAfterChildren() throws IOException {
        Create.Values<Long> create = Create.of(1L, 2L, 3L);
        GenerateSequence createChild = GenerateSequence.from(0);
        PCollection<Long> pt = pipeline.apply(create);
        String userName = "my_transform";
        String childUserName = "my_transform/my_nesting";
        AppliedPTransform<?, ?, ?> transform = AppliedPTransform.of(userName, pipeline.begin().expand(), pt.expand(), create, pipeline);
        AppliedPTransform<?, ?, ?> childTransform = AppliedPTransform.of(childUserName, pipeline.begin().expand(), pt.expand(), createChild, pipeline);
        String childId = components.registerPTransform(childTransform, Collections.emptyList());
        String parentId = components.registerPTransform(transform, Collections.singletonList(childTransform));
        Components components = this.components.toComponents();
        Assert.assertThat(components.getTransformsOrThrow(parentId).getSubtransforms(0), Matchers.equalTo(childId));
        Assert.assertThat(components.getTransformsOrThrow(childId).getSubtransformsCount(), Matchers.equalTo(0));
    }

    @Test
    public void registerTransformEmptyFullName() throws IOException {
        Create.Values<Integer> create = Create.of(1, 2, 3);
        PCollection<Integer> pt = pipeline.apply(create);
        AppliedPTransform<?, ?, ?> transform = AppliedPTransform.of("", pipeline.begin().expand(), pt.expand(), create, pipeline);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(transform.toString());
        components.getExistingPTransformId(transform);
    }

    @Test
    public void registerTransformNullComponents() throws IOException {
        Create.Values<Integer> create = Create.of(1, 2, 3);
        PCollection<Integer> pt = pipeline.apply(create);
        String userName = "my_transform/my_nesting";
        AppliedPTransform<?, ?, ?> transform = AppliedPTransform.of(userName, pipeline.begin().expand(), pt.expand(), create, pipeline);
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("child nodes may not be null");
        components.registerPTransform(transform, null);
    }

    /**
     * Tests that trying to register a transform which has unregistered children throws.
     */
    @Test
    public void registerTransformWithUnregisteredChildren() throws IOException {
        Create.Values<Long> create = Create.of(1L, 2L, 3L);
        GenerateSequence createChild = GenerateSequence.from(0);
        PCollection<Long> pt = pipeline.apply(create);
        String userName = "my_transform";
        String childUserName = "my_transform/my_nesting";
        AppliedPTransform<?, ?, ?> transform = AppliedPTransform.of(userName, pipeline.begin().expand(), pt.expand(), create, pipeline);
        AppliedPTransform<?, ?, ?> childTransform = AppliedPTransform.of(childUserName, pipeline.begin().expand(), pt.expand(), createChild, pipeline);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(childTransform.toString());
        components.registerPTransform(transform, Collections.singletonList(childTransform));
    }

    @Test
    public void registerPCollection() throws IOException {
        PCollection<Long> pCollection = pipeline.apply(GenerateSequence.from(0)).setName("foo");
        String id = components.registerPCollection(pCollection);
        Assert.assertThat(id, Matchers.equalTo("foo"));
        components.toComponents().getPcollectionsOrThrow(id);
    }

    @Test
    public void registerPCollectionExistingNameCollision() throws IOException {
        PCollection<Long> pCollection = pipeline.apply("FirstCount", GenerateSequence.from(0)).setName("foo");
        String firstId = components.registerPCollection(pCollection);
        PCollection<Long> duplicate = pipeline.apply("SecondCount", GenerateSequence.from(0)).setName("foo");
        String secondId = components.registerPCollection(duplicate);
        Assert.assertThat(firstId, Matchers.equalTo("foo"));
        Assert.assertThat(secondId, Matchers.containsString("foo"));
        Assert.assertThat(secondId, Matchers.not(Matchers.equalTo("foo")));
        components.toComponents().getPcollectionsOrThrow(firstId);
        components.toComponents().getPcollectionsOrThrow(secondId);
    }

    @Test
    public void registerWindowingStrategy() throws IOException {
        WindowingStrategy<?, ?> strategy = WindowingStrategy.globalDefault().withMode(ACCUMULATING_FIRED_PANES);
        String name = components.registerWindowingStrategy(strategy);
        Assert.assertThat(name, Matchers.not(Matchers.isEmptyOrNullString()));
        components.toComponents().getWindowingStrategiesOrThrow(name);
    }

    @Test
    public void registerWindowingStrategyIdEqualStrategies() throws IOException {
        WindowingStrategy<?, ?> strategy = WindowingStrategy.globalDefault().withMode(ACCUMULATING_FIRED_PANES);
        String name = components.registerWindowingStrategy(strategy);
        String duplicateName = components.registerWindowingStrategy(WindowingStrategy.globalDefault().withMode(ACCUMULATING_FIRED_PANES));
        Assert.assertThat(name, Matchers.equalTo(duplicateName));
    }
}
