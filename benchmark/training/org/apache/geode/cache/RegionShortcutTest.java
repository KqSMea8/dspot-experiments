/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.cache;


import RegionShortcut.LOCAL;
import RegionShortcut.LOCAL_HEAP_LRU;
import RegionShortcut.LOCAL_OVERFLOW;
import RegionShortcut.LOCAL_PERSISTENT;
import RegionShortcut.PARTITION_HEAP_LRU;
import RegionShortcut.PARTITION_PERSISTENT_OVERFLOW;
import RegionShortcut.REPLICATE_PERSISTENT;
import org.junit.Test;


public class RegionShortcutTest {
    @Test
    public void isPersistent() {
        assertThat(LOCAL.isPersistent()).isFalse();
        assertThat(LOCAL_HEAP_LRU.isPersistent()).isFalse();
        assertThat(LOCAL_OVERFLOW.isPersistent()).isFalse();
        assertThat(LOCAL_PERSISTENT.isPersistent()).isTrue();
        assertThat(PARTITION_PERSISTENT_OVERFLOW.isPersistent()).isTrue();
        assertThat(PARTITION_HEAP_LRU.isPersistent()).isFalse();
        assertThat(REPLICATE_PERSISTENT.isPersistent()).isTrue();
    }
}
