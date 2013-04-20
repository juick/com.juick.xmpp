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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class StreamServer extends Stream {

    public StreamServer(JID from, JID to, InputStream is, OutputStream os) {
        super(from, to, is, os);
    }

    @Override
    public void openStream() throws XmlPullParserException, IOException {
        String msg = "<?xml version='1.0'?><stream:stream xmlns:stream='http://etherx.jabber.org/streams' xmlns='jabber:server' xmlns:db='jabber:server:dialback' to='" + to.toString() + "' version='1.0'>";
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
        String sid = parser.getAttributeValue(null, "id");
        String sfrom = parser.getAttributeValue(null, "from");
        if (sfrom == null || !sfrom.equals(to.toString())) {
            loggedIn = false;
            for (Iterator<StreamListener> it = listenersXmpp.iterator(); it.hasNext();) {
                it.next().onStreamFail("stream:stream, failed authentication");
            }
            return;
        }

        for (Iterator<StreamListener> it = listenersXmpp.iterator(); it.hasNext();) {
            it.next().onStreamReady();
        }
    }
}
