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


import R.dimen.test_dimen;
import android.graphics.drawable.Drawable;
import com.facebook.litho.drawable.ComparableDrawableWrapper;
import com.facebook.litho.reference.Reference;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(ComponentsTestRunner.class)
public class ResolveAttributeTest {
    private ComponentContext mContext;

    @Test
    public void testResolveDrawableAttribute() {
        Column column = Column.create(mContext).backgroundAttr(testAttrDrawable, 0).build();
        InternalNode node = Layout.create(mContext, column);
        Drawable d = mContext.getResources().getDrawable(test_bg);
        ComparableDrawableWrapper comparable = ((ComparableDrawableWrapper) (Reference.acquire(mContext.getAndroidContext(), node.getBackground())));
        assertThat(shadowOf(comparable.getWrappedDrawable()).getCreatedFromResId()).isEqualTo(shadowOf(d).getCreatedFromResId());
    }

    @Test
    public void testResolveDimenAttribute() {
        Column column = Column.create(mContext).widthAttr(testAttrDimen, default_dimen).build();
        InternalNode node = Layout.create(mContext, column);
        node.calculateLayout();
        int dimen = mContext.getResources().getDimensionPixelSize(test_dimen);
        assertThat(((int) (node.getWidth()))).isEqualTo(dimen);
    }

    @Test
    public void testDefaultDrawableAttribute() {
        Column column = Column.create(mContext).backgroundAttr(undefinedAttrDrawable, test_bg).build();
        InternalNode node = Layout.create(mContext, column);
        Drawable d = mContext.getResources().getDrawable(test_bg);
        ComparableDrawableWrapper comparable = ((ComparableDrawableWrapper) (Reference.acquire(mContext.getAndroidContext(), node.getBackground())));
        assertThat(shadowOf(comparable.getWrappedDrawable()).getCreatedFromResId()).isEqualTo(shadowOf(d).getCreatedFromResId());
    }

    @Test
    public void testDefaultDimenAttribute() {
        Column column = Column.create(mContext).widthAttr(undefinedAttrDimen, test_dimen).build();
        InternalNode node = Layout.create(mContext, column);
        node.calculateLayout();
        int dimen = mContext.getResources().getDimensionPixelSize(test_dimen);
        assertThat(((int) (node.getWidth()))).isEqualTo(dimen);
    }

    @Test
    public void testFloatDimenWidthAttribute() {
        Column column = Column.create(mContext).widthAttr(undefinedAttrDimen, test_dimen_float).build();
        InternalNode node = Layout.create(mContext, column);
        node.calculateLayout();
        int dimen = mContext.getResources().getDimensionPixelSize(test_dimen_float);
        assertThat(node.getWidth()).isEqualTo(dimen);
    }

    @Test
    public void testFloatDimenPaddingAttribute() {
        Column column = Column.create(mContext).paddingAttr(LEFT, undefinedAttrDimen, test_dimen_float).build();
        InternalNode node = Layout.create(mContext, column);
        node.calculateLayout();
        int dimen = mContext.getResources().getDimensionPixelSize(test_dimen_float);
        assertThat(node.getPaddingLeft()).isEqualTo(dimen);
    }
}
