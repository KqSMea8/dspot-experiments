/**
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.codegen;


import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;


public class PropertyTest {
    @Test
    public void equals_and_hashCode() {
        Type typeModel = new com.mysema.codegen.model.SimpleType(TypeCategory.ENTITY, "com.querydsl.DomainClass", "com.querydsl", "DomainClass", false, false);
        EntityType type = new EntityType(typeModel);
        Property p1 = new Property(type, "property", type, Collections.<String>emptyList());
        Property p2 = new Property(type, "property", type, Collections.<String>emptyList());
        Assert.assertEquals(p1, p1);
        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void escapedName() {
        Type typeModel = new com.mysema.codegen.model.SimpleType(TypeCategory.ENTITY, "com.querydsl.DomainClass", "com.querydsl", "DomainClass", false, false);
        EntityType type = new EntityType(typeModel);
        Property property = new Property(type, "boolean", type, Collections.<String>emptyList());
        Assert.assertEquals("boolean$", property.getEscapedName());
    }
}
