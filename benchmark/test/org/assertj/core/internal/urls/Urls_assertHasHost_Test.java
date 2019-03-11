/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2019 the original author or authors.
 */
package org.assertj.core.internal.urls;


import java.net.MalformedURLException;
import java.net.URL;
import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.assertj.core.error.uri.ShouldHaveHost;
import org.assertj.core.internal.UrlsBaseTest;
import org.assertj.core.test.TestData;
import org.assertj.core.test.TestFailures;
import org.assertj.core.util.FailureMessages;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class Urls_assertHasHost_Test extends UrlsBaseTest {
    @Test
    public void should_fail_if_actual_is_null() {
        Assertions.assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> urls.assertHasHost(info, null, "www.helloworld.org")).withMessage(FailureMessages.actualIsNull());
    }

    @Test
    public void should_pass_if_actual_URL_has_the_given_host() throws MalformedURLException {
        urls.assertHasHost(info, new URL("http://www.helloworld.org"), "www.helloworld.org");
    }

    @Test
    public void should_pass_if_actual_URL_with_path_has_the_given_host() throws MalformedURLException {
        urls.assertHasHost(info, new URL("http://www.helloworld.org/pages"), "www.helloworld.org");
    }

    @Test
    public void should_fail_if_actual_URL_has_not_the_expected_host() throws MalformedURLException {
        AssertionInfo info = TestData.someInfo();
        URL url = new URL("http://example.com/pages/");
        String expectedHost = "example.org";
        try {
            urls.assertHasHost(info, url, expectedHost);
        } catch (AssertionError e) {
            Mockito.verify(failures).failure(info, ShouldHaveHost.shouldHaveHost(url, expectedHost));
            return;
        }
        TestFailures.failBecauseExpectedAssertionErrorWasNotThrown();
    }
}
