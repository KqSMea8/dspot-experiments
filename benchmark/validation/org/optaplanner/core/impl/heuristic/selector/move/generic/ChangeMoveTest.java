/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.optaplanner.core.impl.heuristic.selector.move.generic;


import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.TestdataEntity;
import org.optaplanner.core.impl.testdata.domain.TestdataSolution;
import org.optaplanner.core.impl.testdata.domain.TestdataValue;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarEntity;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataMultiVarSolution;
import org.optaplanner.core.impl.testdata.domain.multivar.TestdataOtherValue;
import org.optaplanner.core.impl.testdata.domain.valuerange.entityproviding.TestdataEntityProvidingEntity;
import org.optaplanner.core.impl.testdata.domain.valuerange.entityproviding.TestdataEntityProvidingSolution;
import org.optaplanner.core.impl.testdata.util.PlannerAssert;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;


public class ChangeMoveTest {
    @Test
    public void isMoveDoable() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        ScoreDirector<TestdataEntityProvidingSolution> scoreDirector = Mockito.mock(ScoreDirector.class);
        EntityDescriptor<TestdataEntityProvidingSolution> entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();
        ChangeMove<TestdataEntityProvidingSolution> aMove = new ChangeMove(a, entityDescriptor.getGenuineVariableDescriptor("value"), v2);
        a.setValue(v1);
        Assert.assertEquals(true, aMove.isMoveDoable(scoreDirector));
        a.setValue(v2);
        Assert.assertEquals(false, aMove.isMoveDoable(scoreDirector));
        a.setValue(v3);
        Assert.assertEquals(true, aMove.isMoveDoable(scoreDirector));
    }

    @Test
    public void doMove() {
        TestdataValue v1 = new TestdataValue("1");
        TestdataValue v2 = new TestdataValue("2");
        TestdataValue v3 = new TestdataValue("3");
        TestdataEntityProvidingEntity a = new TestdataEntityProvidingEntity("a", Arrays.asList(v1, v2, v3), null);
        ScoreDirector<TestdataEntityProvidingSolution> scoreDirector = Mockito.mock(ScoreDirector.class);
        EntityDescriptor<TestdataEntityProvidingSolution> entityDescriptor = TestdataEntityProvidingEntity.buildEntityDescriptor();
        ChangeMove<TestdataEntityProvidingSolution> aMove = new ChangeMove(a, entityDescriptor.getGenuineVariableDescriptor("value"), v2);
        a.setValue(v1);
        aMove.doMove(scoreDirector);
        Assert.assertEquals(v2, a.getValue());
        a.setValue(v2);
        aMove.doMove(scoreDirector);
        Assert.assertEquals(v2, a.getValue());
        a.setValue(v3);
        aMove.doMove(scoreDirector);
        Assert.assertEquals(v2, a.getValue());
    }

    @Test
    public void rebase() {
        GenuineVariableDescriptor<TestdataSolution> variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataEntity e1 = new TestdataEntity("e1", v1);
        TestdataEntity e2 = new TestdataEntity("e2", null);
        TestdataEntity e3 = new TestdataEntity("e3", v1);
        TestdataValue destinationV1 = new TestdataValue("v1");
        TestdataValue destinationV2 = new TestdataValue("v2");
        TestdataEntity destinationE1 = new TestdataEntity("e1", destinationV1);
        TestdataEntity destinationE2 = new TestdataEntity("e2", null);
        TestdataEntity destinationE3 = new TestdataEntity("e3", destinationV1);
        ScoreDirector<TestdataSolution> destinationScoreDirector = PlannerTestUtils.mockRebasingScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor(), new Object[][]{ new Object[]{ v1, destinationV1 }, new Object[]{ v2, destinationV2 }, new Object[]{ e1, destinationE1 }, new Object[]{ e2, destinationE2 }, new Object[]{ e3, destinationE3 } });
        assertSameProperties(destinationE1, null, new ChangeMove(e1, variableDescriptor, null).rebase(destinationScoreDirector));
        assertSameProperties(destinationE1, destinationV1, new ChangeMove(e1, variableDescriptor, v1).rebase(destinationScoreDirector));
        assertSameProperties(destinationE2, null, new ChangeMove(e2, variableDescriptor, null).rebase(destinationScoreDirector));
        assertSameProperties(destinationE3, destinationV2, new ChangeMove(e3, variableDescriptor, v2).rebase(destinationScoreDirector));
    }

    @Test
    public void getters() {
        ChangeMove<TestdataMultiVarSolution> move = new ChangeMove(new TestdataMultiVarEntity("a"), TestdataMultiVarEntity.buildVariableDescriptorForPrimaryValue(), null);
        PlannerAssert.assertCode("a", move.getEntity());
        Assert.assertEquals("primaryValue", move.getVariableName());
        PlannerAssert.assertCode(null, move.getToPlanningValue());
        move = new ChangeMove(new TestdataMultiVarEntity("b"), TestdataMultiVarEntity.buildVariableDescriptorForSecondaryValue(), new TestdataValue("1"));
        PlannerAssert.assertCode("b", move.getEntity());
        Assert.assertEquals("secondaryValue", move.getVariableName());
        PlannerAssert.assertCode("1", move.getToPlanningValue());
    }

    @Test
    public void toStringTest() {
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataEntity a = new TestdataEntity("a", null);
        TestdataEntity b = new TestdataEntity("b", v1);
        GenuineVariableDescriptor<TestdataSolution> variableDescriptor = TestdataEntity.buildVariableDescriptorForValue();
        Assert.assertEquals("a {null -> null}", new ChangeMove(a, variableDescriptor, null).toString());
        Assert.assertEquals("a {null -> v1}", new ChangeMove(a, variableDescriptor, v1).toString());
        Assert.assertEquals("a {null -> v2}", new ChangeMove(a, variableDescriptor, v2).toString());
        Assert.assertEquals("b {v1 -> null}", new ChangeMove(b, variableDescriptor, null).toString());
        Assert.assertEquals("b {v1 -> v1}", new ChangeMove(b, variableDescriptor, v1).toString());
        Assert.assertEquals("b {v1 -> v2}", new ChangeMove(b, variableDescriptor, v2).toString());
    }

    @Test
    public void toStringTestMultiVar() {
        TestdataValue v1 = new TestdataValue("v1");
        TestdataValue v2 = new TestdataValue("v2");
        TestdataValue v3 = new TestdataValue("v3");
        TestdataValue v4 = new TestdataValue("v4");
        TestdataOtherValue w1 = new TestdataOtherValue("w1");
        TestdataOtherValue w2 = new TestdataOtherValue("w2");
        TestdataMultiVarEntity a = new TestdataMultiVarEntity("a", null, null, null);
        TestdataMultiVarEntity b = new TestdataMultiVarEntity("b", v1, v3, w1);
        TestdataMultiVarEntity c = new TestdataMultiVarEntity("c", v2, v4, w2);
        EntityDescriptor<TestdataMultiVarSolution> entityDescriptor = TestdataMultiVarEntity.buildEntityDescriptor();
        GenuineVariableDescriptor<TestdataMultiVarSolution> variableDescriptor = entityDescriptor.getGenuineVariableDescriptor("secondaryValue");
        Assert.assertEquals("a {null -> null}", new ChangeMove(a, variableDescriptor, null).toString());
        Assert.assertEquals("a {null -> v1}", new ChangeMove(a, variableDescriptor, v1).toString());
        Assert.assertEquals("a {null -> v2}", new ChangeMove(a, variableDescriptor, v2).toString());
        Assert.assertEquals("b {v3 -> null}", new ChangeMove(b, variableDescriptor, null).toString());
        Assert.assertEquals("b {v3 -> v1}", new ChangeMove(b, variableDescriptor, v1).toString());
        Assert.assertEquals("b {v3 -> v2}", new ChangeMove(b, variableDescriptor, v2).toString());
        Assert.assertEquals("c {v4 -> v3}", new ChangeMove(c, variableDescriptor, v3).toString());
    }
}
