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
package com.facebook.litho.specmodels.model;


import ClassNames.COMPONENT_LIFECYCLE_MOUNT_TYPE_NONE;
import ClassNames.COMPONENT_LIFECYCLE_MOUNT_TYPE_VIEW;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Tests {@link SpecModelValidation}
 */
public class SpecModelValidationTest {
    private final SpecModel mSpecModel = Mockito.mock(SpecModel.class);

    private final MountSpecModel mMountSpecModel = Mockito.mock(MountSpecModel.class);

    private final Object mModelRepresentedObject = new Object();

    private final Object mMountSpecModelRepresentedObject = new Object();

    @Test
    public void testNameValidation() {
        Mockito.when(mSpecModel.getSpecName()).thenReturn("testNotEndingWithSpecXXXX");
        List<SpecModelValidationError> validationErrors = SpecModelValidation.validateName(mSpecModel);
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0).element).isSameAs(mModelRepresentedObject);
        assertThat(validationErrors.get(0).message).isEqualTo(("You must suffix the class name of your spec with \"Spec\" e.g. a \"MyComponentSpec\" " + "class name generates a component named \"MyComponent\"."));
    }

    @Test
    public void testMountTypeValidation() {
        Mockito.when(mMountSpecModel.getMountType()).thenReturn(COMPONENT_LIFECYCLE_MOUNT_TYPE_NONE);
        List<SpecModelValidationError> validationErrors = SpecModelValidation.validateGetMountType(mMountSpecModel);
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0).element).isSameAs(mMountSpecModelRepresentedObject);
        assertThat(validationErrors.get(0).message).isEqualTo("onCreateMountContent's return type should be either a View or a Drawable subclass.");
    }

    @Test
    public void testDisplayListValidation() {
        Mockito.when(mMountSpecModel.shouldUseDisplayList()).thenReturn(true);
        Mockito.when(mMountSpecModel.getMountType()).thenReturn(COMPONENT_LIFECYCLE_MOUNT_TYPE_VIEW);
        List<SpecModelValidationError> validationErrors = SpecModelValidation.validateShouldUseDisplayLists(mMountSpecModel);
        assertThat(validationErrors).hasSize(1);
        assertThat(validationErrors.get(0).element).isSameAs(mMountSpecModelRepresentedObject);
        assertThat(validationErrors.get(0).message).isEqualTo("shouldUseDisplayList = true can only be used on MountSpecs that mount a drawable.");
    }
}
