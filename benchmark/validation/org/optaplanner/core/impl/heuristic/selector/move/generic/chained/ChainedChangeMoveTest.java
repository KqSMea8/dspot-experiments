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
package org.optaplanner.core.impl.heuristic.selector.move.generic.chained;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.domain.variable.inverserelation.SingletonInverseVariableSupply;
import org.optaplanner.core.impl.heuristic.selector.SelectorTestUtils;
import org.optaplanner.core.impl.score.director.InnerScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedAnchor;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedEntity;
import org.optaplanner.core.impl.testdata.domain.chained.TestdataChainedSolution;
import org.optaplanner.core.impl.testdata.util.PlannerTestUtils;


public class ChainedChangeMoveTest {
    @Test
    public void noTrailing() {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity.buildVariableDescriptorForChainedObject();
        InnerScoreDirector<TestdataChainedSolution> scoreDirector = PlannerTestUtils.mockScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());
        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity b1 = new TestdataChainedEntity("b1", b0);
        SingletonInverseVariableSupply inverseVariableSupply = SelectorTestUtils.mockSingletonInverseVariableSupply(new TestdataChainedEntity[]{ a1, a2, a3, b1 });
        ChainedChangeMove<TestdataChainedSolution> move = new ChainedChangeMove(a3, variableDescriptor, inverseVariableSupply, b1);
        Assert.assertEquals(true, move.isMoveDoable(scoreDirector));
        ChainedChangeMove<TestdataChainedSolution> undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2);
        SelectorTestUtils.assertChain(b0, b1, a3);
        Mockito.verify(scoreDirector).changeVariableFacade(variableDescriptor, a3, b1);
        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3);
        SelectorTestUtils.assertChain(b0, b1);
    }

    @Test
    public void oldAndNewTrailing() {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity.buildVariableDescriptorForChainedObject();
        InnerScoreDirector<TestdataChainedSolution> scoreDirector = PlannerTestUtils.mockScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());
        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity b1 = new TestdataChainedEntity("b1", b0);
        SingletonInverseVariableSupply inverseVariableSupply = SelectorTestUtils.mockSingletonInverseVariableSupply(new TestdataChainedEntity[]{ a1, a2, a3, b1 });
        ChainedChangeMove<TestdataChainedSolution> move = new ChainedChangeMove(a2, variableDescriptor, inverseVariableSupply, b0);
        Assert.assertEquals(true, move.isMoveDoable(scoreDirector));
        ChainedChangeMove<TestdataChainedSolution> undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a3);
        SelectorTestUtils.assertChain(b0, a2, b1);
        Mockito.verify(scoreDirector).changeVariableFacade(variableDescriptor, a2, b0);
        Mockito.verify(scoreDirector).changeVariableFacade(variableDescriptor, a3, a1);
        Mockito.verify(scoreDirector).changeVariableFacade(variableDescriptor, b1, a2);
        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3);
        SelectorTestUtils.assertChain(b0, b1);
    }

    @Test
    public void sameChainWithOneBetween() {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity.buildVariableDescriptorForChainedObject();
        InnerScoreDirector<TestdataChainedSolution> scoreDirector = PlannerTestUtils.mockScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());
        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        SingletonInverseVariableSupply inverseVariableSupply = SelectorTestUtils.mockSingletonInverseVariableSupply(new TestdataChainedEntity[]{ a1, a2, a3, a4 });
        ChainedChangeMove<TestdataChainedSolution> move = new ChainedChangeMove(a2, variableDescriptor, inverseVariableSupply, a3);
        Assert.assertEquals(true, move.isMoveDoable(scoreDirector));
        ChainedChangeMove<TestdataChainedSolution> undoMove = move.createUndoMove(scoreDirector);
        move.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a3, a2, a4);
        Mockito.verify(scoreDirector).changeVariableFacade(variableDescriptor, a2, a3);
        Mockito.verify(scoreDirector).changeVariableFacade(variableDescriptor, a3, a1);
        Mockito.verify(scoreDirector).changeVariableFacade(variableDescriptor, a4, a2);
        undoMove.doMove(scoreDirector);
        SelectorTestUtils.assertChain(a0, a1, a2, a3, a4);
    }

    @Test
    public void sameChainWithItself() {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity.buildVariableDescriptorForChainedObject();
        InnerScoreDirector<TestdataChainedSolution> scoreDirector = PlannerTestUtils.mockScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());
        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        SingletonInverseVariableSupply inverseVariableSupply = SelectorTestUtils.mockSingletonInverseVariableSupply(new TestdataChainedEntity[]{ a1, a2, a3, a4 });
        ChainedChangeMove<TestdataChainedSolution> move = new ChainedChangeMove(a2, variableDescriptor, inverseVariableSupply, a2);
        Assert.assertEquals(false, move.isMoveDoable(scoreDirector));
    }

    @Test
    public void sameChainWithSamePlanningValue() {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity.buildVariableDescriptorForChainedObject();
        InnerScoreDirector<TestdataChainedSolution> scoreDirector = PlannerTestUtils.mockScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor());
        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedEntity a3 = new TestdataChainedEntity("a3", a2);
        TestdataChainedEntity a4 = new TestdataChainedEntity("a4", a3);
        SingletonInverseVariableSupply inverseVariableSupply = SelectorTestUtils.mockSingletonInverseVariableSupply(new TestdataChainedEntity[]{ a1, a2, a3, a4 });
        ChainedChangeMove<TestdataChainedSolution> move = new ChainedChangeMove(a2, variableDescriptor, inverseVariableSupply, a1);
        Assert.assertEquals(false, move.isMoveDoable(scoreDirector));
    }

    @Test
    public void rebase() {
        GenuineVariableDescriptor<TestdataChainedSolution> variableDescriptor = TestdataChainedEntity.buildVariableDescriptorForChainedObject();
        TestdataChainedAnchor a0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity a1 = new TestdataChainedEntity("a1", a0);
        TestdataChainedEntity a2 = new TestdataChainedEntity("a2", a1);
        TestdataChainedAnchor b0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity c1 = new TestdataChainedEntity("c1", null);
        TestdataChainedAnchor destinationA0 = new TestdataChainedAnchor("a0");
        TestdataChainedEntity destinationA1 = new TestdataChainedEntity("a1", destinationA0);
        TestdataChainedEntity destinationA2 = new TestdataChainedEntity("a2", destinationA1);
        TestdataChainedAnchor destinationB0 = new TestdataChainedAnchor("b0");
        TestdataChainedEntity destinationC1 = new TestdataChainedEntity("c1", null);
        ScoreDirector<TestdataChainedSolution> destinationScoreDirector = PlannerTestUtils.mockRebasingScoreDirector(variableDescriptor.getEntityDescriptor().getSolutionDescriptor(), new Object[][]{ new Object[]{ a0, destinationA0 }, new Object[]{ a1, destinationA1 }, new Object[]{ a2, destinationA2 }, new Object[]{ b0, destinationB0 }, new Object[]{ c1, destinationC1 } });
        SingletonInverseVariableSupply inverseVariableSupply = Mockito.mock(SingletonInverseVariableSupply.class);
        assertSameProperties(destinationA1, null, new ChainedChangeMove(a1, variableDescriptor, inverseVariableSupply, null).rebase(destinationScoreDirector));
        assertSameProperties(destinationA2, destinationB0, new ChainedChangeMove(a2, variableDescriptor, inverseVariableSupply, b0).rebase(destinationScoreDirector));
        assertSameProperties(destinationC1, destinationA2, new ChainedChangeMove(c1, variableDescriptor, inverseVariableSupply, a2).rebase(destinationScoreDirector));
    }
}
