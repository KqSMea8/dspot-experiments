/**
 * Copyright 2017 Paul Schaub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smackx.jingle;


import Jingle.Builder;
import JingleAction.session_initiate;
import junit.framework.TestCase;
import org.jivesoftware.smack.test.util.SmackTestSuite;
import org.jivesoftware.smackx.jingle.element.Jingle;
import org.junit.Test;
import org.jxmpp.jid.FullJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;


/**
 * Test the Jingle class.
 */
public class JingleTest extends SmackTestSuite {
    @Test(expected = IllegalArgumentException.class)
    public void emptyBuilderTest() {
        Jingle.Builder builder = Jingle.getBuilder();
        builder.build();
    }

    @Test(expected = NullPointerException.class)
    public void onlySessionIdBuilderTest() {
        String sessionId = "testSessionId";
        Jingle.Builder builder = Jingle.getBuilder();
        builder.setSessionId(sessionId);
        builder.build();
    }

    @Test
    public void parserTest() throws XmppStringprepException {
        String sessionId = "testSessionId";
        Jingle.Builder builder = Jingle.getBuilder();
        builder.setSessionId(sessionId);
        builder.setAction(session_initiate);
        FullJid romeo = JidCreate.fullFrom("romeo@montague.lit/orchard");
        FullJid juliet = JidCreate.fullFrom("juliet@capulet.lit/balcony");
        builder.setInitiator(romeo);
        builder.setResponder(juliet);
        Jingle jingle = builder.build();
        TestCase.assertNotNull(jingle);
        TestCase.assertEquals(romeo, jingle.getInitiator());
        TestCase.assertEquals(juliet, jingle.getResponder());
        TestCase.assertEquals(jingle.getAction(), session_initiate);
        TestCase.assertEquals(sessionId, jingle.getSid());
        String xml = ((("<jingle xmlns='urn:xmpp:jingle:1' " + ((("initiator='romeo@montague.lit/orchard' " + "responder='juliet@capulet.lit/balcony' ") + "action='session-initiate' ") + "sid='")) + sessionId) + "'>") + "</jingle>";
        TestCase.assertTrue(jingle.toXML().toString().contains(xml));
    }
}
