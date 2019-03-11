/**
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ehcache.impl.internal.sizeof;


import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.config.store.heap.DefaultSizeOfEngineConfiguration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 *
 * @author Abhilash
 */
public class DefaultSizeOfEngineConfigurationTest {
    @Test
    public void testIllegalMaxObjectSizeArgument() {
        try {
            new DefaultSizeOfEngineConfiguration(0, MemoryUnit.B, 1L);
            Assert.fail();
        } catch (Exception illegalArgument) {
            MatcherAssert.assertThat(illegalArgument, Matchers.instanceOf(IllegalArgumentException.class));
            MatcherAssert.assertThat(illegalArgument.getMessage(), Matchers.equalTo("ObjectGraphSize/ObjectSize can only accept positive values."));
        }
    }

    @Test
    public void testIllegalMaxObjectGraphSizeArgument() {
        try {
            new DefaultSizeOfEngineConfiguration(1L, MemoryUnit.B, 0);
            Assert.fail();
        } catch (Exception illegalArgument) {
            MatcherAssert.assertThat(illegalArgument, Matchers.instanceOf(IllegalArgumentException.class));
            MatcherAssert.assertThat(illegalArgument.getMessage(), Matchers.equalTo("ObjectGraphSize/ObjectSize can only accept positive values."));
        }
    }

    @Test
    public void testValidArguments() {
        DefaultSizeOfEngineConfiguration configuration = new DefaultSizeOfEngineConfiguration(10L, MemoryUnit.B, 10L);
        MatcherAssert.assertThat(configuration.getMaxObjectGraphSize(), Matchers.equalTo(10L));
        MatcherAssert.assertThat(configuration.getMaxObjectSize(), Matchers.equalTo(10L));
        MatcherAssert.assertThat(configuration.getUnit(), Matchers.equalTo(MemoryUnit.B));
    }
}
