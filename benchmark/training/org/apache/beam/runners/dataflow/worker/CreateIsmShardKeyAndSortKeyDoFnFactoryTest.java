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
package org.apache.beam.runners.dataflow.worker;


import GlobalWindow.INSTANCE;
import PropertyNames.ENCODING;
import PropertyNames.OBJECT_TYPE_NAME;
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.runners.dataflow.internal.IsmFormat.IsmRecordCoder;
import org.apache.beam.runners.dataflow.util.CloudObject;
import org.apache.beam.runners.dataflow.util.CloudObjects;
import org.apache.beam.runners.dataflow.worker.util.common.worker.ParDoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableList;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Tests for {@link CreateIsmShardKeyAndSortKeyDoFnFactory}.
 */
@RunWith(JUnit4.class)
public class CreateIsmShardKeyAndSortKeyDoFnFactoryTest {
    @Test
    public void testConversionOfRecord() throws Exception {
        ParDoFn parDoFn = /* pipeline options */
        /* side input infos */
        /* main output tag */
        /* output tag to receiver index */
        /* exection context */
        /* operation context */
        new CreateIsmShardKeyAndSortKeyDoFnFactory().create(null, CloudObject.fromSpec(ImmutableMap.of(OBJECT_TYPE_NAME, "CreateIsmShardKeyAndSortKeyDoFn", ENCODING, createIsmRecordEncoding())), null, null, null, null, null);
        List<Object> outputReceiver = new ArrayList<>();
        parDoFn.startBundle(outputReceiver::add);
        parDoFn.processElement(valueInGlobalWindow(KV.of(42, 43)));
        IsmRecordCoder<?> coder = ((IsmRecordCoder) (CloudObjects.coderFromCloudObject(CloudObject.fromSpec(createIsmRecordEncoding()))));
        Assert.assertThat(outputReceiver, Matchers.contains(valueInGlobalWindow(/* hash key */
        KV.of(coder.hash(ImmutableList.of(42)), /* sort key */
        /* value */
        KV.of(KV.of(42, INSTANCE), 43)))));
    }
}
