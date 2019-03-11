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
package org.apache.geode.internal.cache.tier.sockets;


import org.apache.geode.CopyHelper;
import org.apache.geode.internal.util.BlobHelper;
import org.apache.geode.test.junit.categories.ClientServerTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;


@Category({ ClientServerTest.class })
public class ObjectPartListJUnitTest {
    @Test
    public void testValueAsObject() throws Exception {
        VersionedObjectList list = new VersionedObjectList(100, false, false);
        byte[] normalBytes = "value1".getBytes();
        list.addObjectPart("key", normalBytes, false, null);
        list.addObjectPart("key", "value2", true, null);
        byte[] serializedObjectBytes = BlobHelper.serializeToBlob("value3");
        list.addObjectPart("key", serializedObjectBytes, true, null);
        list.addExceptionPart("key", new ObjectPartListJUnitTest.AssertionError("hello"));
        list.addObjectPartForAbsentKey("key", null);
        // Create a clone of the this.
        VersionedObjectList newList = CopyHelper.copy(list);
        checkObjectValues(newList);
        // THIS TEST FAILS! ObjectPartArrayList doesn't
        // preserve all its state when it is serialized (it loses track of type information
        // for values of type BYTES by writing the type info as OBJECT). However,
        // we'll have to leave it to avoid breaking old clients.
        // create another copy, just to double check
        // newList = CopyHelper.copy(newList);
        // 
        // checkObjectValues(newList);
    }

    @Test
    public void testValueAsObjectByteArray() throws Exception {
        ObjectPartList list = new VersionedObjectList(100, false, false, true);
        byte[] normalBytes = "value1".getBytes();
        list.addObjectPart("key", normalBytes, false, null);
        list.addObjectPart("key", "value2", true, null);
        byte[] serializedObjectBytes = BlobHelper.serializeToBlob("value3");
        list.addObjectPart("key", serializedObjectBytes, true, null);
        list.addExceptionPart("key", new ObjectPartListJUnitTest.AssertionError("hello"));
        list.addObjectPartForAbsentKey("key", null);
        // Create a clone of the this list.
        ObjectPartList newList = CopyHelper.copy(list);
        checkSerializedValues(newList);
        // Create another copy, just to double check
        // all the info was perserved
        newList = CopyHelper.copy(newList);
        checkSerializedValues(newList);
    }

    private static class AssertionError extends Exception {
        public AssertionError(String message) {
            super(message);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ObjectPartListJUnitTest.AssertionError)) {
                return false;
            }
            if (!(((ObjectPartListJUnitTest.AssertionError) (o)).getMessage().equals(getMessage()))) {
                return false;
            }
            return true;
        }
    }
}
