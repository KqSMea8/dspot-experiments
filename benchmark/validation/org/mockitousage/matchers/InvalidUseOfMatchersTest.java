/**
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.matchers;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.StateMaster;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockitousage.IMethods;


@RunWith(MockitoJUnitRunner.Silent.class)
public class InvalidUseOfMatchersTest {
    private IMethods mock = Mockito.mock(IMethods.class);

    @Test
    public void should_detect_wrong_number_of_matchers_when_stubbing() {
        Mockito.when(mock.threeArgumentMethod(1, "2", "3")).thenReturn(null);
        try {
            Mockito.when(mock.threeArgumentMethod(1, ArgumentMatchers.eq("2"), "3")).thenReturn(null);
            Assert.fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage()).contains("3 matchers expected").contains("1 recorded");
        }
    }

    @Test
    public void should_detect_stupid_use_of_matchers_when_verifying() {
        mock.oneArg(true);
        ArgumentMatchers.eq("that's the stupid way");
        ArgumentMatchers.eq("of using matchers");
        try {
            Mockito.verify(mock).oneArg(true);
            Assert.fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage()).contains("Misplaced or misused argument matcher detected here");
            e.printStackTrace();
        }
    }

    @Test
    public void should_not_scream_on_correct_usage() throws Exception {
        mock.simpleMethod(AdditionalMatchers.not(ArgumentMatchers.eq("asd")));
        mock.simpleMethod(AdditionalMatchers.or(ArgumentMatchers.eq("jkl"), ArgumentMatchers.eq("asd")));
    }

    @Test
    public void should_scream_when_no_matchers_inside_not() {
        try {
            mock.simpleMethod(AdditionalMatchers.not("jkl"));
            Assert.fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage()).contains("No matchers found for").containsIgnoringCase("Not(?)");
        }
    }

    @Test
    public void should_scream_when_not_enough_matchers_inside_or_AddtionalMatcher() {
        try {
            mock.simpleMethod(AdditionalMatchers.or(ArgumentMatchers.eq("jkl"), "asd"));
            Assert.fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage()).containsIgnoringCase("inside additional matcher Or(?)").contains("2 sub matchers expected").contains("1 recorded");
        }
    }

    @Test
    public void should_scream_when_Matchers_count_dont_match_parameter_count() {
        try {
            mock.threeArgumentMethod(1, "asd", ArgumentMatchers.eq("asd"));
            Assert.fail();
        } catch (InvalidUseOfMatchersException e) {
            assertThat(e.getMessage()).contains("3 matchers expected").contains("1 recorded");
        }
    }

    @Test
    public void should_mention_matcher_when_misuse_detected() {
        // Given
        // When
        Result run = new JUnitCore().run(InvalidUseOfMatchersTest.ObjectMatcherMisuseOnPrimitiveSite.class);
        // Then
        assertThat(run.getFailures()).hasSize(2);
        assertThat(run.getFailures().get(0).getException()).isInstanceOf(NullPointerException.class).hasMessage(null);
        assertThat(run.getFailures().get(1).getException()).isInstanceOf(InvalidUseOfMatchersException.class).hasMessageContaining("primitive alternatives");
        new StateMaster().reset();
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class ObjectMatcherMisuseOnPrimitiveSite {
        @Test
        public void fails_with_NPE() {
            IMethods mock = Mockito.mock(IMethods.class);
            Mockito.doNothing().when(mock).twoArgumentMethod(ArgumentMatchers.eq(73), ((Integer) (ArgumentMatchers.any())));// <= Raise NPE on this call site

        }
    }
}
