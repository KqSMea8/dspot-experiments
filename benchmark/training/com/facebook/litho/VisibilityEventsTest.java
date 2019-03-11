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


import YogaEdge.TOP;
import android.graphics.Rect;
import android.widget.FrameLayout;
import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestViewComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(ComponentsTestRunner.class)
public class VisibilityEventsTest {
    private static final int LEFT = 0;

    private static final int RIGHT = 10;

    private ComponentContext mContext;

    private LithoView mLithoView;

    private FrameLayout mParent;

    @Test
    public void testVisibleEvent() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibleHandler(visibleEventHandler).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 5);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 10), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
    }

    @Test
    public void testVisibleEventWithHeightRatio() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibleHeightRatio(0.4F).visibleHandler(visibleEventHandler).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 10);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 1), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 2), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 3), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 4), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 5), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 6), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 7), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
    }

    @Test
    public void testVisibleEventWithWidthRatio() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibleWidthRatio(0.4F).visibleHandler(visibleEventHandler).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 5);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, 3, 10), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, 5, 10), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
    }

    @Test
    public void testVisibleEventWithHeightAndWidthRatio() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibleWidthRatio(0.4F).visibleHeightRatio(0.4F).visibleHandler(visibleEventHandler).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 10);
        content.getDispatchedEventHandlers().clear();
        // Neither width or height are in visible range
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, 3, 6), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        // Width but not height are in visible range
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, 5, 6), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        // Height but not width are in visible range
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, 3, 8), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        // Height and width are both in visible range
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, 5, 8), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
    }

    @Test
    public void testFocusedOccupiesHalfViewport() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<FocusedVisibleEvent> focusedEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).focusedHandler(focusedEventHandler).widthPx(10).heightPx(10)).build(), true, 10, 10);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 4), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(focusedEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 5), true);
        assertThat(content.getDispatchedEventHandlers()).contains(focusedEventHandler);
    }

    @Test
    public void testFocusedOccupiesLessThanHalfViewport() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<FocusedVisibleEvent> focusedEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).focusedHandler(focusedEventHandler).widthPx(10).heightPx(3)).build(), true, 10, 10);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 2), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(focusedEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 3), true);
        assertThat(content.getDispatchedEventHandlers()).contains(focusedEventHandler);
    }

    @Test
    public void testMultipleFocusAndUnfocusEvents() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<FocusedVisibleEvent> focusedHandler = new EventHandler(content, 2);
        final EventHandler<UnfocusedVisibleEvent> unfocusedHandler = new EventHandler(content, 3);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).focusedHandler(focusedHandler).unfocusedHandler(unfocusedHandler).widthPx(10).heightPx(7).marginPx(TOP, 3)).build(), true, 100, 100);
        lithoView.performIncrementalMount(new Rect(0, 0, 0, 0), true);
        // Mount test view in the middle of the view port (focused)
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 4, VisibilityEventsTest.RIGHT, 10), true);
        assertThat(content.getDispatchedEventHandlers()).containsOnly(focusedHandler);
        // Mount test view on the edge of the viewport (not focused)
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 9, VisibilityEventsTest.RIGHT, 14), true);
        assertThat(content.getDispatchedEventHandlers()).containsOnly(unfocusedHandler);
        // Mount test view in the middle of the view port (focused)
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 3, VisibilityEventsTest.RIGHT, 9), true);
        assertThat(content.getDispatchedEventHandlers()).containsOnly(focusedHandler);
        // Mount test view on the edge of the viewport (not focused)
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 1, VisibilityEventsTest.RIGHT, 6), true);
        assertThat(content.getDispatchedEventHandlers()).containsOnly(unfocusedHandler);
    }

    @Test
    public void testFullImpressionEvent() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<FullImpressionVisibleEvent> fullImpressionVisibleEvent = new EventHandler(content, 2);
        mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).fullImpressionHandler(fullImpressionVisibleEvent).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 10);
        assertThat(content.getDispatchedEventHandlers()).contains(fullImpressionVisibleEvent);
    }

    @Test
    public void testInvisibleEvent() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<InvisibleEvent> invisibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).invisibleHandler(invisibleEventHandler).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 10);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(invisibleEventHandler);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 5), true);
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler);
    }

    @Test
    public void testVisibleRectChangedEventItemVisible() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibilityChangedEvent> visibilityChangedHandler = new EventHandler(content, 3);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibilityChangedHandler(visibilityChangedHandler).widthPx(10).heightPx(10)).build(), true, 10, 10);
        VisibilityChangedEvent visibilityChangedEvent = ((VisibilityChangedEvent) (content.getEventState(visibilityChangedHandler)));
        assertThat(visibilityChangedEvent.visibleHeight).isEqualTo(10);
        assertThat(visibilityChangedEvent.visibleWidth).isEqualTo(10);
        assertThat(visibilityChangedEvent.percentVisibleHeight).isEqualTo(100.0F);
        assertThat(visibilityChangedEvent.percentVisibleWidth).isEqualTo(100.0F);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 4), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
        visibilityChangedEvent = ((VisibilityChangedEvent) (content.getEventState(visibilityChangedHandler)));
        assertThat(visibilityChangedEvent.visibleHeight).isEqualTo(4);
        assertThat(visibilityChangedEvent.visibleWidth).isEqualTo(10);
        assertThat(visibilityChangedEvent.percentVisibleHeight).isEqualTo(40.0F);
        assertThat(visibilityChangedEvent.percentVisibleWidth).isEqualTo(100.0F);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 5), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
        visibilityChangedEvent = ((VisibilityChangedEvent) (content.getEventState(visibilityChangedHandler)));
        assertThat(visibilityChangedEvent.visibleHeight).isEqualTo(5);
        assertThat(visibilityChangedEvent.visibleWidth).isEqualTo(10);
        assertThat(visibilityChangedEvent.percentVisibleHeight).isEqualTo(50.0F);
        assertThat(visibilityChangedEvent.percentVisibleWidth).isEqualTo(100.0F);
    }

    @Test
    public void testVisibleRectChangedEventItemNotVisible() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibilityChangedEvent> visibilityChangedHandler = new EventHandler(content, 3);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibilityChangedHandler(visibilityChangedHandler).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 10);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 5), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
        VisibilityChangedEvent visibilityChangedEvent = ((VisibilityChangedEvent) (content.getEventState(visibilityChangedHandler)));
        assertThat(visibilityChangedEvent.visibleHeight).isEqualTo(0);
        assertThat(visibilityChangedEvent.visibleWidth).isEqualTo(0);
        assertThat(visibilityChangedEvent.percentVisibleHeight).isEqualTo(0.0F);
        assertThat(visibilityChangedEvent.percentVisibleWidth).isEqualTo(0.0F);
    }

    @Test
    public void testVisibleRectChangedEventLargeView() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibilityChangedEvent> visibilityChangedHandler = new EventHandler(content, 3);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibilityChangedHandler(visibilityChangedHandler).widthPx(10).heightPx(10)).build(), true, 10, 1000);
        VisibilityChangedEvent visibilityChangedEvent = ((VisibilityChangedEvent) (content.getEventState(visibilityChangedHandler)));
        assertThat(visibilityChangedEvent.visibleHeight).isEqualTo(10);
        assertThat(visibilityChangedEvent.visibleWidth).isEqualTo(10);
        assertThat(visibilityChangedEvent.percentVisibleHeight).isEqualTo(100.0F);
        assertThat(visibilityChangedEvent.percentVisibleWidth).isEqualTo(100.0F);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 4), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
        visibilityChangedEvent = ((VisibilityChangedEvent) (content.getEventState(visibilityChangedHandler)));
        assertThat(visibilityChangedEvent.visibleHeight).isEqualTo(4);
        assertThat(visibilityChangedEvent.visibleWidth).isEqualTo(10);
        assertThat(visibilityChangedEvent.percentVisibleHeight).isEqualTo(40.0F);
        assertThat(visibilityChangedEvent.percentVisibleWidth).isEqualTo(100.0F);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 5), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
        visibilityChangedEvent = ((VisibilityChangedEvent) (content.getEventState(visibilityChangedHandler)));
        assertThat(visibilityChangedEvent.visibleHeight).isEqualTo(5);
        assertThat(visibilityChangedEvent.visibleWidth).isEqualTo(10);
        assertThat(visibilityChangedEvent.percentVisibleHeight).isEqualTo(50.0F);
        assertThat(visibilityChangedEvent.percentVisibleWidth).isEqualTo(100.0F);
    }

    @Test
    public void testVisibleAndInvisibleEvents() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(content, 1);
        final EventHandler<InvisibleEvent> invisibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibleHandler(visibleEventHandler).invisibleHandler(invisibleEventHandler).widthPx(10).heightPx(5).marginPx(TOP, 5)).build(), true, 10, 10);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(invisibleEventHandler);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 5), true);
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 3, VisibilityEventsTest.RIGHT, 9), true);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(invisibleEventHandler);
        content.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 10, VisibilityEventsTest.RIGHT, 15), true);
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
    }

    @Test
    public void testMultipleVisibleEvents() {
        final TestComponent content1 = TestViewComponent.create(mContext).build();
        final TestComponent content2 = TestViewComponent.create(mContext).build();
        final TestComponent content3 = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler1 = new EventHandler(content1, 1);
        final EventHandler<VisibleEvent> visibleEventHandler2 = new EventHandler(content2, 2);
        final EventHandler<VisibleEvent> visibleEventHandler3 = new EventHandler(content3, 3);
        final EventHandler<VisibilityChangedEvent> visibilityChangedHandler = new EventHandler(content3, 4);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(content1).visibleHandler(visibleEventHandler1).widthPx(10).heightPx(5)).child(Wrapper.create(mContext).delegate(content2).visibleHandler(visibleEventHandler2).widthPx(10).heightPx(5)).child(Wrapper.create(mContext).delegate(content3).visibleHandler(visibleEventHandler3).visibilityChangedHandler(visibilityChangedHandler).widthPx(10).heightPx(5)).build(), true, 10, 10);
        assertThat(content1.getDispatchedEventHandlers()).contains(visibleEventHandler1);
        assertThat(content2.getDispatchedEventHandlers()).contains(visibleEventHandler2);
        assertThat(content3.getDispatchedEventHandlers()).contains(visibleEventHandler3);
        assertThat(content3.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
        content1.getDispatchedEventHandlers().clear();
        content2.getDispatchedEventHandlers().clear();
        content3.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 0), true);
        assertThat(content1.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler1);
        assertThat(content2.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler2);
        assertThat(content3.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler3);
        assertThat(content3.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
        content1.getDispatchedEventHandlers().clear();
        content2.getDispatchedEventHandlers().clear();
        content3.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 3), true);
        assertThat(content1.getDispatchedEventHandlers()).contains(visibleEventHandler1);
        assertThat(content2.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler2);
        assertThat(content3.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler3);
        assertThat(content3.getDispatchedEventHandlers()).doesNotContain(visibilityChangedHandler);
        content1.getDispatchedEventHandlers().clear();
        content2.getDispatchedEventHandlers().clear();
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 3, VisibilityEventsTest.RIGHT, 11), true);
        assertThat(content1.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler1);
        assertThat(content2.getDispatchedEventHandlers()).contains(visibleEventHandler2);
        assertThat(content3.getDispatchedEventHandlers()).contains(visibleEventHandler3);
        assertThat(content3.getDispatchedEventHandlers()).contains(visibilityChangedHandler);
    }

    @Test
    public void testDetachWithReleasedTreeTriggersInvisibilityItems() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<InvisibleEvent> invisibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).invisibleHandler(invisibleEventHandler).widthPx(10).heightPx(10)).build(), true);
        lithoView.performIncrementalMount(new Rect(VisibilityEventsTest.LEFT, 0, VisibilityEventsTest.RIGHT, 10), true);
        lithoView.release();
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(invisibleEventHandler);
        unbindComponent(lithoView);
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler);
    }

    @Test
    public void testSetComponentWithDifferentKeyGeneratesVisibilityEvents() {
        final TestComponent component1 = TestViewComponent.create(mContext).key("component1").build();
        final EventHandler<VisibleEvent> visibleEventHandler1 = new EventHandler(component1, 1);
        final EventHandler<InvisibleEvent> invisibleEventHandler1 = new EventHandler(component1, 2);
        final EventHandler<FocusedVisibleEvent> focusedEventHandler1 = new EventHandler(component1, 3);
        final EventHandler<UnfocusedVisibleEvent> unfocusedEventHandler1 = new EventHandler(component1, 4);
        final LithoView lithoView = mountComponent(mContext, Column.create(mContext).child(Wrapper.create(mContext).delegate(component1).visibleHandler(visibleEventHandler1).invisibleHandler(invisibleEventHandler1).focusedHandler(focusedEventHandler1).unfocusedHandler(unfocusedEventHandler1).widthPx(10).heightPx(10)).build(), true);
        assertThat(component1.getDispatchedEventHandlers()).contains(visibleEventHandler1);
        assertThat(component1.getDispatchedEventHandlers()).contains(focusedEventHandler1);
        final TestComponent component2 = TestViewComponent.create(mContext).key("component2").build();
        final EventHandler<VisibleEvent> visibleEventHandler2 = new EventHandler(component2, 3);
        lithoView.setComponentTree(ComponentTree.create(mContext, Column.create(mContext).child(Wrapper.create(mContext).delegate(component2).visibleHandler(visibleEventHandler2).widthPx(10).heightPx(10)).build()).build());
        measureAndLayout(lithoView);
        assertThat(component1.getDispatchedEventHandlers()).contains(invisibleEventHandler1);
        assertThat(component1.getDispatchedEventHandlers()).contains(unfocusedEventHandler1);
        assertThat(component2.getDispatchedEventHandlers()).contains(visibleEventHandler2);
    }

    @Test
    public void testTransientStateDoesNotTriggerVisibilityEvents() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(content, 2);
        final LithoView lithoView = mountComponent(mContext, Column.create(mContext).child(Wrapper.create(mContext).delegate(content).visibleHandler(visibleEventHandler).widthPx(10).heightPx(10)).build(), true);
        lithoView.performIncrementalMount(new Rect(0, (-10), 10, (-5)), true);
        content.getDispatchedEventHandlers().clear();
        lithoView.setHasTransientState(true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.setMountStateDirty();
        lithoView.performIncrementalMount(new Rect(0, (-10), 10, (-5)), true);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        lithoView.setHasTransientState(false);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
    }

    @Test
    public void testRemovingComponentTriggersInvisible() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(content, 1);
        final EventHandler<InvisibleEvent> invisibleEventHandler = new EventHandler(content, 2);
        final Component wrappedContent = Wrapper.create(mContext).delegate(content).widthPx(10).heightPx(5).visibleHandler(visibleEventHandler).invisibleHandler(invisibleEventHandler).build();
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(wrappedContent).build(), true, 10, 10);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(invisibleEventHandler);
        content.getDispatchedEventHandlers().clear();
        lithoView.setComponent(Column.create(mContext).build());
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(visibleEventHandler);
        content.getDispatchedEventHandlers().clear();
        lithoView.setComponent(Column.create(mContext).child(wrappedContent).build());
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler);
        assertThat(content.getDispatchedEventHandlers()).doesNotContain(invisibleEventHandler);
    }

    @Test
    public void testMultipleVisibilityEventsOnSameNode() {
        final TestComponent content = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler1 = new EventHandler(content, 1);
        final EventHandler<VisibleEvent> visibleEventHandler2 = new EventHandler(content, 2);
        final EventHandler<VisibleEvent> visibleEventHandler3 = new EventHandler(content, 3);
        final EventHandler<InvisibleEvent> invisibleEventHandler1 = new EventHandler(content, 4);
        final EventHandler<InvisibleEvent> invisibleEventHandler2 = new EventHandler(content, 5);
        final EventHandler<InvisibleEvent> invisibleEventHandler3 = new EventHandler(content, 6);
        final EventHandler<FocusedVisibleEvent> focusedEventHandler1 = new EventHandler(content, 7);
        final EventHandler<FocusedVisibleEvent> focusedEventHandler2 = new EventHandler(content, 8);
        final EventHandler<FocusedVisibleEvent> focusedEventHandler3 = new EventHandler(content, 9);
        final EventHandler<UnfocusedVisibleEvent> unfocusedEventHandler1 = new EventHandler(content, 10);
        final EventHandler<UnfocusedVisibleEvent> unfocusedEventHandler2 = new EventHandler(content, 11);
        final EventHandler<UnfocusedVisibleEvent> unfocusedEventHandler3 = new EventHandler(content, 12);
        final EventHandler<FullImpressionVisibleEvent> fullImpressionVisibleEventHandler1 = new EventHandler(content, 13);
        final EventHandler<FullImpressionVisibleEvent> fullImpressionVisibleEventHandler2 = new EventHandler(content, 14);
        final EventHandler<FullImpressionVisibleEvent> fullImpressionVisibleEventHandler3 = new EventHandler(content, 15);
        final LithoView lithoView = mountComponent(mContext, mLithoView, Column.create(mContext).child(Wrapper.create(mContext).delegate(Wrapper.create(mContext).delegate(Wrapper.create(mContext).delegate(content).visibleHandler(visibleEventHandler1).invisibleHandler(invisibleEventHandler1).focusedHandler(focusedEventHandler1).unfocusedHandler(unfocusedEventHandler1).fullImpressionHandler(fullImpressionVisibleEventHandler1).widthPx(10).heightPx(5).build()).visibleHandler(visibleEventHandler2).invisibleHandler(invisibleEventHandler2).focusedHandler(focusedEventHandler2).unfocusedHandler(unfocusedEventHandler2).fullImpressionHandler(fullImpressionVisibleEventHandler2).build()).visibleHandler(visibleEventHandler3).invisibleHandler(invisibleEventHandler3).focusedHandler(focusedEventHandler3).unfocusedHandler(unfocusedEventHandler3).fullImpressionHandler(fullImpressionVisibleEventHandler3)).build(), true, 10, 10);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler1);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler2);
        assertThat(content.getDispatchedEventHandlers()).contains(visibleEventHandler3);
        assertThat(content.getDispatchedEventHandlers()).contains(focusedEventHandler1);
        assertThat(content.getDispatchedEventHandlers()).contains(focusedEventHandler2);
        assertThat(content.getDispatchedEventHandlers()).contains(focusedEventHandler3);
        assertThat(content.getDispatchedEventHandlers()).contains(fullImpressionVisibleEventHandler1);
        assertThat(content.getDispatchedEventHandlers()).contains(fullImpressionVisibleEventHandler2);
        assertThat(content.getDispatchedEventHandlers()).contains(fullImpressionVisibleEventHandler3);
        content.getDispatchedEventHandlers().clear();
        unbindComponent(lithoView);
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler1);
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler2);
        assertThat(content.getDispatchedEventHandlers()).contains(invisibleEventHandler3);
        assertThat(content.getDispatchedEventHandlers()).contains(unfocusedEventHandler1);
        assertThat(content.getDispatchedEventHandlers()).contains(unfocusedEventHandler2);
        assertThat(content.getDispatchedEventHandlers()).contains(unfocusedEventHandler3);
    }

    @Test
    public void testSetVisibilityHint() {
        final TestComponent component = TestViewComponent.create(mContext).build();
        final EventHandler<VisibleEvent> visibleEventHandler = new EventHandler(component, 1);
        final EventHandler<InvisibleEvent> invisibleEventHandler = new EventHandler(component, 2);
        final EventHandler<FocusedVisibleEvent> focusedEventHandler = new EventHandler(component, 3);
        final EventHandler<UnfocusedVisibleEvent> unfocusedEventHandler = new EventHandler(component, 4);
        final EventHandler<FullImpressionVisibleEvent> fullImpressionHandler = new EventHandler(component, 5);
        final LithoView lithoView = mountComponent(mContext, Column.create(mContext).child(Wrapper.create(mContext).delegate(component).visibleHandler(visibleEventHandler).invisibleHandler(invisibleEventHandler).focusedHandler(focusedEventHandler).unfocusedHandler(unfocusedEventHandler).fullImpressionHandler(fullImpressionHandler).widthPx(10).heightPx(10)).build(), true);
        assertThat(component.getDispatchedEventHandlers()).contains(visibleEventHandler);
        assertThat(component.getDispatchedEventHandlers()).contains(focusedEventHandler);
        assertThat(component.getDispatchedEventHandlers()).contains(fullImpressionHandler);
        component.getDispatchedEventHandlers().clear();
        lithoView.setVisibilityHint(false);
        assertThat(component.getDispatchedEventHandlers()).contains(invisibleEventHandler);
        assertThat(component.getDispatchedEventHandlers()).contains(unfocusedEventHandler);
        component.getDispatchedEventHandlers().clear();
        lithoView.setVisibilityHint(true);
        assertThat(component.getDispatchedEventHandlers()).contains(visibleEventHandler);
        assertThat(component.getDispatchedEventHandlers()).contains(focusedEventHandler);
        assertThat(component.getDispatchedEventHandlers()).contains(fullImpressionHandler);
    }
}
