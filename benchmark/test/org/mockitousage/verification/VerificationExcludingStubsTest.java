/**
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.verification;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;


@SuppressWarnings("unchecked")
public class VerificationExcludingStubsTest extends TestBase {
    @Mock
    IMethods mock;

    @Test
    public void shouldAllowToExcludeStubsForVerification() throws Exception {
        // given
        Mockito.when(mock.simpleMethod()).thenReturn("foo");
        // when
        String stubbed = mock.simpleMethod();// irrelevant call because it is stubbing

        mock.objectArgMethod(stubbed);
        // then
        Mockito.verify(mock).objectArgMethod("foo");
        // verifyNoMoreInteractions fails:
        try {
            Mockito.verifyNoMoreInteractions(mock);
            Assert.fail();
        } catch (NoInteractionsWanted e) {
        }
        // but it works when stubs are ignored:
        Mockito.ignoreStubs(mock);
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test
    public void shouldExcludeFromVerificationInOrder() throws Exception {
        // given
        Mockito.when(mock.simpleMethod()).thenReturn("foo");
        // when
        mock.objectArgMethod("1");
        mock.objectArgMethod("2");
        mock.simpleMethod();// calling the stub

        // then
        InOrder inOrder = Mockito.inOrder(Mockito.ignoreStubs(mock));
        inOrder.verify(mock).objectArgMethod("1");
        inOrder.verify(mock).objectArgMethod("2");
        inOrder.verifyNoMoreInteractions();
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test(expected = NotAMockException.class)
    public void shouldIgnoringStubsDetectNulls() throws Exception {
        Mockito.ignoreStubs(mock, null);
    }

    @Test(expected = NotAMockException.class)
    public void shouldIgnoringStubsDetectNonMocks() throws Exception {
        Mockito.ignoreStubs(mock, new Object());
    }
}
