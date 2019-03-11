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
package com.querydsl.apt.domain;


import QQueryEmbeddable2Test_User.user.complex.a;
import com.querydsl.core.annotations.QueryEmbeddable;
import com.querydsl.core.annotations.QueryEntity;
import org.junit.Assert;
import org.junit.Test;


public class QueryEmbeddable2Test {
    @QueryEntity
    public static class User {
        QueryEmbeddable2Test.Complex<String> complex;
    }

    @QueryEmbeddable
    public static class Complex<T extends Comparable<T>> implements Comparable<QueryEmbeddable2Test.Complex<T>> {
        T a;

        @Override
        public int compareTo(QueryEmbeddable2Test.Complex<T> arg0) {
            return 0;
        }

        public boolean equals(Object o) {
            return o == (this);
        }
    }

    @Test
    public void user_complex_a() {
        Assert.assertNotNull(a);
    }
}
