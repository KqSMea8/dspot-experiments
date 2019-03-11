/**
 * -\-\-
 * Spotify Apollo API Implementations
 * --
 * Copyright (C) 2013 - 2015 Spotify AB
 * --
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
 * -/-/-
 */
package com.spotify.apollo.meta;


import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;


public class ApplicationOrMetaRouterTest {
    @Test
    public void shouldMatchMeta() {
        String uri = "gopher://au.th/_meta/0/info";
        MatcherAssert.assertThat(ApplicationOrMetaRouter.isMeta(uri), Is.is(true));
    }
}
