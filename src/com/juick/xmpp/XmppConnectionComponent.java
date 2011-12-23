/*
 * Juick
 * Copyright (C) 2008-2011, Ugnich Anton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.juick.xmpp;

import java.io.IOException;
import java.util.Enumeration;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class XmppConnectionComponent extends XmppConnection {

    public XmppConnectionComponent(final JID jid, final String password, final String server, final int port, final boolean use_ssl) {
        super(jid, password, server, port, use_ssl);
    }

    @Override
    public void login() throws XmlPullParserException, IOException {
        String msg = "<stream:stream xmlns='jabber:component:accept' xmlns:stream='http://etherx.jabber.org/streams' to='" + jid.toString() + "'>";
        writer.write(msg);

        parser.next(); // stream:stream

        String id = parser.getAttributeValue(null, "id");
        String from = parser.getAttributeValue(null, "from");
        if (from == null || !from.equals(jid.toString())) {
            loggedIn = false;
            for (Enumeration e = listenersXmpp.elements(); e.hasMoreElements();) {
                XmppListener xl = (XmppListener) e.nextElement();
                xl.onAuthFailed("stream:stream, failed authentication");
            }
            return;
        }

        msg = "<handshake>" + SHA1.encode(id + password) + "</handshake>";
        writer.write(msg);

        parser.next();
        if (parser.getName().equals("handshake")) {
            parser.next();
            loggedIn = true;
        } else {
            loggedIn = false;
            for (Enumeration e = listenersXmpp.elements(); e.hasMoreElements();) {
                XmppListener xl = (XmppListener) e.nextElement();
                xl.onAuthFailed(parser.getName() + ", failed authentication");
            }
            return;
        }
    }
}
