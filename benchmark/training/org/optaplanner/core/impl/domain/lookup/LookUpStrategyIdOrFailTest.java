/**
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.core.impl.domain.lookup;


import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.impl.testdata.domain.lookup.TestdataObjectEnum;
import org.optaplanner.core.impl.testdata.domain.lookup.TestdataObjectId;
import org.optaplanner.core.impl.testdata.domain.lookup.TestdataObjectMultipleIds;
import org.optaplanner.core.impl.testdata.domain.lookup.TestdataObjectNoId;


public class LookUpStrategyIdOrFailTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private LookUpManager lookUpManager;

    @Test
    public void addRemoveWithId() {
        TestdataObjectId object = new TestdataObjectId(0);
        lookUpManager.addWorkingObject(object);
        lookUpManager.removeWorkingObject(object);
        // The removed object cannot be looked up
        Assert.assertNull(lookUpManager.lookUpWorkingObjectOrReturnNull(object));
    }

    @Test
    public void addRemoveEnum() {
        TestdataObjectEnum object = TestdataObjectEnum.THIRD_VALUE;
        lookUpManager.addWorkingObject(object);
        lookUpManager.removeWorkingObject(object);
    }

    @Test
    public void addWithNullId() {
        TestdataObjectId object = new TestdataObjectId(null);
        expectedException.expect(IllegalArgumentException.class);
        lookUpManager.addWorkingObject(object);
    }

    @Test
    public void removeWithNullId() {
        TestdataObjectId object = new TestdataObjectId(null);
        expectedException.expect(IllegalArgumentException.class);
        lookUpManager.removeWorkingObject(object);
    }

    @Test
    public void addWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        expectedException.expect(IllegalArgumentException.class);
        lookUpManager.addWorkingObject(object);
    }

    @Test
    public void removeWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        expectedException.expect(IllegalArgumentException.class);
        lookUpManager.removeWorkingObject(object);
    }

    @Test
    public void addSameIdTwice() {
        TestdataObjectId object = new TestdataObjectId(2);
        lookUpManager.addWorkingObject(object);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(" have the same planningId ");
        expectedException.expectMessage(object.toString());
        lookUpManager.addWorkingObject(new TestdataObjectId(2));
    }

    @Test
    public void removeWithoutAdding() {
        TestdataObjectId object = new TestdataObjectId(0);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("differ");
        lookUpManager.removeWorkingObject(object);
    }

    @Test
    public void lookUpWithId() {
        TestdataObjectId object = new TestdataObjectId(1);
        lookUpManager.addWorkingObject(object);
        Assert.assertSame(object, lookUpManager.lookUpWorkingObject(new TestdataObjectId(1)));
    }

    @Test
    public void lookUpWithoutId() {
        TestdataObjectNoId object = new TestdataObjectNoId();
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(("does not have a " + (PlanningId.class.getSimpleName())));
        lookUpManager.lookUpWorkingObject(object);
    }

    @Test
    public void lookUpWithoutAdding() {
        TestdataObjectId object = new TestdataObjectId(0);
        Assert.assertNull(lookUpManager.lookUpWorkingObjectOrReturnNull(object));
    }

    @Test
    public void addWithTwoIds() {
        TestdataObjectMultipleIds object = new TestdataObjectMultipleIds();
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("3 members");
        expectedException.expectMessage(PlanningId.class.getSimpleName());
        lookUpManager.addWorkingObject(object);
    }

    @Test
    public void removeWithTwoIds() {
        TestdataObjectMultipleIds object = new TestdataObjectMultipleIds();
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("3 members");
        expectedException.expectMessage(PlanningId.class.getSimpleName());
        lookUpManager.removeWorkingObject(object);
    }
}
