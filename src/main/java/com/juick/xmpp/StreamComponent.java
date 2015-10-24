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

import com.juick.xmpp.utils.SHA1;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class StreamComponent extends Stream {

    String password;

    public StreamComponent(JID to, InputStream is, OutputStream os, String password) {
        super(null, to, is, os);
        this.password = password;
    }

    @Override
    public void openStream() throws XmlPullParserException, IOException {
        String msg = "<stream:stream xmlns='jabber:component:accept' xmlns:stream='http://etherx.jabber.org/streams' to='" + to.toString() + "'>";
        writer.write(msg);
        writer.flush();

        parser.next(); // stream:stream
        String sid = parser.getAttributeValue(null, "id");
        String sfrom = parser.getAttributeValue(null, "from");
        if (sfrom == null || !sfrom.equals(to.toString())) {
            loggedIn = false;
            for (Iterator<StreamListener> it = listenersStream.iterator(); it.hasNext();) {
                it.next().onStreamFail("stream:stream, failed authentication");
            }
            return;
        }

        msg = "<handshake>" + SHA1.encode(sid + password) + "</handshake>";
        writer.write(msg);
        writer.flush();

        parser.next();
        if (parser.getName().equals("handshake")) {
            parser.next();
            loggedIn = true;
            for (Iterator<StreamListener> it = listenersStream.iterator(); it.hasNext();) {
                it.next().onStreamReady();
            }
        } else {
            loggedIn = false;
            for (Iterator<StreamListener> it = listenersStream.iterator(); it.hasNext();) {
                it.next().onStreamFail(parser.getName() + ", failed authentication");
            }
        }
    }
}
