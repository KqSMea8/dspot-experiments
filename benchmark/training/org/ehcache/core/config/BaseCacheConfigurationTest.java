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
package org.ehcache.core.config;


import org.ehcache.config.ResourcePools;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;


/**
 * BaseCacheConfigurationTest
 */
public class BaseCacheConfigurationTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testThrowsWithNullKeyType() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("keyType");
        new BaseCacheConfiguration<>(null, String.class, null, null, null, Mockito.mock(ResourcePools.class));
    }

    @Test
    public void testThrowsWithNullValueType() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("valueType");
        new BaseCacheConfiguration<>(Long.class, null, null, null, null, Mockito.mock(ResourcePools.class));
    }

    @Test
    public void testThrowsWithNullResourcePools() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("resourcePools");
        new BaseCacheConfiguration<>(Long.class, String.class, null, null, null, null);
    }
}
