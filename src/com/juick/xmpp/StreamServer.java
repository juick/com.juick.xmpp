/*
 * Juick
 * Copyright (C) 2008-2013, Ugnich Anton
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
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class StreamServer extends Stream {

    public StreamServer(final JID jid, final String password, final String server, final int port, final boolean use_ssl) {
        super(jid, password, server, port, use_ssl);
    }

    @Override
    public void login() throws XmlPullParserException, IOException {
        String msg = "<?xml version='1.0'?><stream:stream xmlns:stream='http://etherx.jabber.org/streams' xmlns='jabber:server' xmlns:db='jabber:server:dialback' to='" + jid.toString() + "' version='1.0'>";
        writer.write(msg);
        writer.flush();
        /*
        <?xml version='1.0'?><stream:stream xmlns:stream='http://etherx.jabber.org/streams' xmlns='jabber:server' xmlns:db='jabber:server:dialback' id='2466825736' version='1.0'>
        <stream:features>
        <starttls xmlns='urn:ietf:params:xml:ns:xmpp-tls'/>
        <c xmlns='http://jabber.org/protocol/caps' hash='sha-1' node='http://www.process-one.net/en/ejabberd/' ver='rvAR01fKsc40hT0hOLGDuG25y9o='/>
        </stream:features>
         */
        parser.next(); // stream:stream
        String id = parser.getAttributeValue(null, "id");
        String from = parser.getAttributeValue(null, "from");
        if (from == null || !from.equals(jid.toString())) {
            loggedIn = false;
            for (Iterator<StreamListener> it = listenersXmpp.iterator(); it.hasNext();) {
                it.next().onAuthFailed("stream:stream, failed authentication");
            }
            return;
        }
    }
}
