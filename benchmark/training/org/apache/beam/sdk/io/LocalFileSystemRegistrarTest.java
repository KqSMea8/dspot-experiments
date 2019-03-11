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
package org.apache.beam.sdk.io;


import java.util.ServiceLoader;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.vendor.guava.v20_0.com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Tests for {@link LocalFileSystemRegistrar}.
 */
@RunWith(JUnit4.class)
public class LocalFileSystemRegistrarTest {
    @Test
    public void testServiceLoader() {
        for (FileSystemRegistrar registrar : Lists.newArrayList(ServiceLoader.load(FileSystemRegistrar.class).iterator())) {
            if (registrar instanceof LocalFileSystemRegistrar) {
                Iterable<FileSystem> fileSystems = registrar.fromOptions(PipelineOptionsFactory.create());
                Assert.assertThat(fileSystems, Matchers.contains(Matchers.instanceOf(LocalFileSystem.class)));
                return;
            }
        }
        Assert.fail(("Expected to find " + (LocalFileSystemRegistrar.class)));
    }
}
