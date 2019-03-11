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
package org.apache.geode.management.internal;


import javax.management.openmbean.CompositeData;
import org.apache.geode.management.internal.OpenTypeConverter.CompositeBuilderViaFrom;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


public class CompositeBuilderViaFromTest {
    @Test
    public void shouldBeMockable() throws Exception {
        CompositeBuilderViaFrom mockCompositeBuilderViaFrom = Mockito.mock(CompositeBuilderViaFrom.class);
        CompositeData compositeData = null;
        String[] itemNames = new String[1];
        OpenTypeConverter[] converters = new OpenTypeConverter[1];
        Object result = new Object();
        Mockito.when(mockCompositeBuilderViaFrom.fromCompositeData(ArgumentMatchers.eq(compositeData), ArgumentMatchers.eq(itemNames), ArgumentMatchers.eq(converters))).thenReturn(result);
        assertThat(mockCompositeBuilderViaFrom.fromCompositeData(compositeData, itemNames, converters)).isSameAs(result);
    }
}
