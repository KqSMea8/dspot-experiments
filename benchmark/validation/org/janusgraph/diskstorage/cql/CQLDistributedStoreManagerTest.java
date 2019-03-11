/**
 * Copyright 2017 JanusGraph Authors
 */
/**
 *
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
/**
 * you may not use this file except in compliance with the License.
 */
/**
 * You may obtain a copy of the License at
 */
/**
 *
 */
/**
 * http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 *
 */
/**
 * Unless required by applicable law or agreed to in writing, software
 */
/**
 * distributed under the License is distributed on an "AS IS" BASIS,
 */
/**
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
/**
 * See the License for the specific language governing permissions and
 */
/**
 * limitations under the License.
 */
package org.janusgraph.diskstorage.cql;


import org.janusgraph.TestCategory;
import org.janusgraph.diskstorage.DistributedStoreManagerTest;
import org.janusgraph.diskstorage.common.DistributedStoreManager.Deployment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class CQLDistributedStoreManagerTest extends DistributedStoreManagerTest<CQLStoreManager> {
    @Override
    @Test
    @Tag(TestCategory.ORDERED_KEY_STORE_TESTS)
    public void testGetDeployment() {
        final Deployment deployment = ((CassandraStorageSetup.HOSTNAME) == null) ? Deployment.LOCAL : Deployment.REMOTE;
        Assertions.assertEquals(deployment, manager.getDeployment());
    }
}
