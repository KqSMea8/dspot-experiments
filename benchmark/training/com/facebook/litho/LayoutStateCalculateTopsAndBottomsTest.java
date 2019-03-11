/**
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.litho;


import LayoutState.sBottomsComparator;
import LayoutState.sTopsComparator;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(ComponentsTestRunner.class)
public class LayoutStateCalculateTopsAndBottomsTest {
    @Test
    public void testCalculateTopsAndBottoms() {
        final Component component = new InlineLayoutSpec() {
            @Override
            protected Component onCreateLayout(ComponentContext c) {
                return Column.create(c).child(Column.create(c).child(TestDrawableComponent.create(c).wrapInView().heightPx(50))).child(TestDrawableComponent.create(c).heightPx(20)).child(TestDrawableComponent.create(c).positionType(ABSOLUTE).positionPx(TOP, 10).positionPx(BOTTOM, 30)).build();
            }
        };
        LayoutState layoutState = LayoutStateCalculateTopsAndBottomsTest.calculateLayoutState(application, component, (-1), SizeSpec.makeSizeSpec(100, SizeSpec.EXACTLY), SizeSpec.makeSizeSpec(100, SizeSpec.AT_MOST));
        assertThat(layoutState.getMountableOutputCount()).isEqualTo(5);
        assertThat(layoutState.getMountableOutputTops().get(0).getBounds().top).isEqualTo(0);
        assertThat(layoutState.getMountableOutputTops().get(1).getBounds().top).isEqualTo(0);
        assertThat(layoutState.getMountableOutputTops().get(2).getBounds().top).isEqualTo(0);
        assertThat(layoutState.getMountableOutputTops().get(3).getBounds().top).isEqualTo(10);
        assertThat(layoutState.getMountableOutputTops().get(4).getBounds().top).isEqualTo(50);
        assertThat(layoutState.getMountableOutputBottoms().get(0).getBounds().bottom).isEqualTo(40);
        assertThat(layoutState.getMountableOutputBottoms().get(1).getBounds().bottom).isEqualTo(50);
        assertThat(layoutState.getMountableOutputBottoms().get(2).getBounds().bottom).isEqualTo(50);
        assertThat(layoutState.getMountableOutputBottoms().get(3).getBounds().bottom).isEqualTo(70);
        assertThat(layoutState.getMountableOutputBottoms().get(4).getBounds().bottom).isEqualTo(70);
        assertThat(layoutState.getMountableOutputAt(2)).isSameAs(layoutState.getMountableOutputTops().get(2));
        assertThat(layoutState.getMountableOutputAt(4)).isSameAs(layoutState.getMountableOutputTops().get(3));
        assertThat(layoutState.getMountableOutputAt(3)).isSameAs(layoutState.getMountableOutputTops().get(4));
        assertThat(layoutState.getMountableOutputAt(4)).isSameAs(layoutState.getMountableOutputBottoms().get(0));
        assertThat(layoutState.getMountableOutputAt(2)).isSameAs(layoutState.getMountableOutputBottoms().get(1));
        assertThat(layoutState.getMountableOutputAt(1)).isSameAs(layoutState.getMountableOutputBottoms().get(2));
        assertThat(layoutState.getMountableOutputAt(3)).isSameAs(layoutState.getMountableOutputBottoms().get(3));
        assertThat(layoutState.getMountableOutputAt(0)).isSameAs(layoutState.getMountableOutputBottoms().get(4));
    }

    @Test
    public void testCalculateTopsAndBottomsWhenEqual() {
        final Component component = new InlineLayoutSpec() {
            @Override
            protected Component onCreateLayout(ComponentContext c) {
                return Column.create(c).child(TestDrawableComponent.create(c).heightPx(50)).child(TestDrawableComponent.create(c).wrapInView().positionType(ABSOLUTE).positionPx(TOP, 0).positionPx(BOTTOM, 0)).build();
            }
        };
        LayoutState layoutState = LayoutStateCalculateTopsAndBottomsTest.calculateLayoutState(application, component, (-1), SizeSpec.makeSizeSpec(100, SizeSpec.EXACTLY), SizeSpec.makeSizeSpec(100, SizeSpec.AT_MOST));
        assertThat(layoutState.getMountableOutputCount()).isEqualTo(4);
        assertThat(layoutState.getMountableOutputTops().get(0).getBounds().top).isEqualTo(0);
        assertThat(layoutState.getMountableOutputTops().get(1).getBounds().top).isEqualTo(0);
        assertThat(layoutState.getMountableOutputTops().get(2).getBounds().top).isEqualTo(0);
        assertThat(layoutState.getMountableOutputTops().get(3).getBounds().top).isEqualTo(0);
        assertThat(layoutState.getMountableOutputBottoms().get(0).getBounds().bottom).isEqualTo(50);
        assertThat(layoutState.getMountableOutputBottoms().get(1).getBounds().bottom).isEqualTo(50);
        assertThat(layoutState.getMountableOutputBottoms().get(2).getBounds().bottom).isEqualTo(50);
        assertThat(layoutState.getMountableOutputBottoms().get(3).getBounds().bottom).isEqualTo(50);
        assertThat(layoutState.getMountableOutputAt(0)).isSameAs(layoutState.getMountableOutputTops().get(0));
        assertThat(layoutState.getMountableOutputAt(1)).isSameAs(layoutState.getMountableOutputTops().get(1));
        assertThat(layoutState.getMountableOutputAt(2)).isSameAs(layoutState.getMountableOutputTops().get(2));
        assertThat(layoutState.getMountableOutputAt(3)).isSameAs(layoutState.getMountableOutputTops().get(3));
        assertThat(layoutState.getMountableOutputAt(0)).isSameAs(layoutState.getMountableOutputBottoms().get(3));
        assertThat(layoutState.getMountableOutputAt(1)).isSameAs(layoutState.getMountableOutputBottoms().get(2));
        assertThat(layoutState.getMountableOutputAt(2)).isSameAs(layoutState.getMountableOutputBottoms().get(1));
        assertThat(layoutState.getMountableOutputAt(3)).isSameAs(layoutState.getMountableOutputBottoms().get(0));
    }

    @Test
    public void testTopsComparatorIsEquivalenceRelation() {
        LayoutOutput[] layoutOutputs = new LayoutOutput[4];
        layoutOutputs[0] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 10, 0);
        layoutOutputs[1] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 10, 1);
        layoutOutputs[2] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 20, 2);
        layoutOutputs[3] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 20, 3);
        // reflexive
        for (LayoutOutput layoutOutput : layoutOutputs) {
            assertThat(LayoutState.sTopsComparator.compare(layoutOutput, layoutOutput)).isEqualTo(0);
        }
        // symmetric
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                assertThat(((-1) * (LayoutState.sTopsComparator.compare(layoutOutputs[j], layoutOutputs[i])))).isEqualTo(LayoutState.sTopsComparator.compare(layoutOutputs[i], layoutOutputs[j]));
            }
        }
        // transitivity
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    if (((i != j) && (j != k)) && (i != k)) {
                        if ((sTopsComparator.compare(layoutOutputs[i], layoutOutputs[j])) == (sTopsComparator.compare(layoutOutputs[j], layoutOutputs[k]))) {
                            assertThat(LayoutState.sTopsComparator.compare(layoutOutputs[j], layoutOutputs[k])).isEqualTo(LayoutState.sTopsComparator.compare(layoutOutputs[i], layoutOutputs[j]));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testBottomsComparatorIsEquivalenceRelation() {
        LayoutOutput[] layoutOutputs = new LayoutOutput[4];
        layoutOutputs[0] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 10, 0);
        layoutOutputs[1] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 10, 1);
        layoutOutputs[2] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 20, 2);
        layoutOutputs[3] = LayoutStateCalculateTopsAndBottomsTest.createLayoutOutput(0, 20, 3);
        // reflexive
        for (LayoutOutput layoutOutput : layoutOutputs) {
            assertThat(LayoutState.sBottomsComparator.compare(layoutOutput, layoutOutput)).isEqualTo(0);
        }
        // symmetric
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                assertThat(((-1) * (LayoutState.sBottomsComparator.compare(layoutOutputs[j], layoutOutputs[i])))).isEqualTo(LayoutState.sBottomsComparator.compare(layoutOutputs[i], layoutOutputs[j]));
            }
        }
        // transitivity
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    if (((i != j) && (j != k)) && (i != k)) {
                        if ((sBottomsComparator.compare(layoutOutputs[i], layoutOutputs[j])) == (sBottomsComparator.compare(layoutOutputs[j], layoutOutputs[k]))) {
                            assertThat(LayoutState.sBottomsComparator.compare(layoutOutputs[j], layoutOutputs[k])).isEqualTo(LayoutState.sBottomsComparator.compare(layoutOutputs[i], layoutOutputs[j]));
                        }
                    }
                }
            }
        }
    }
}
