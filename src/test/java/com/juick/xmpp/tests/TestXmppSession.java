package com.juick.xmpp.tests;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;

public class TestXmppSession extends XmppSession {
    protected TestXmppSession(String xmppServiceDomain, XmppSessionConfiguration configuration, ConnectionConfiguration... connectionConfigurations) {
        super(xmppServiceDomain, configuration, connectionConfigurations);
    }

    public static TestXmppSession create(String xmppServiceDomain, XmppSessionConfiguration configuration) {
        TestXmppSession session = new TestXmppSession(xmppServiceDomain, configuration);
        notifyCreationListeners(session);
        return session;
    }

    @Override
    public void connect(Jid from) throws XmppException {

    }

    @Override
    public Jid getConnectedResource() {
        return null;
    }
}
