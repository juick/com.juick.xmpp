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

import com.juick.xmpp.utils.Base64;
import com.juick.xmpp.extensions.ResourceBinding;
import com.juick.xmpp.extensions.StreamFeatures;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class StreamClient extends Stream implements Iq.IqListener {

    public final static String XMLNS = "urn:ietf:params:xml:ns:xmpp-session";
    String password;

    public StreamClient(JID from, JID to, InputStream is, OutputStream os, String password) {
        super(from, to, is, os);
        this.password = password;
    }

    @Override
    public void openStream() throws XmlPullParserException, IOException {
        String msg = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + to.Host + "' version='1.0'>";
        writer.write(msg);
        writer.flush();
        parser.next(); // stream:stream

        StreamFeatures features = StreamFeatures.parse(parser);
        if (features.STARTTLS == StreamFeatures.REQUIRED || features.PLAIN == StreamFeatures.NOTAVAILABLE) {
            loggedIn = false;
            for (Iterator<StreamListener> it = listenersStream.iterator(); it.hasNext();) {
                it.next().onStreamFail("stream:features, failed authentication");
            }
            return;
        }

        msg = "<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>";
        byte[] auth_msg = (from.Bare() + '\0' + from.Username + '\0' + password).getBytes();
        msg = msg + Base64.encode(auth_msg) + "</auth>";
        writer.write(msg);
        writer.flush();
        parser.next();
        if (parser.getName().equals("success")) {
            do {
                parser.next();
            } while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("success")));
            loggedIn = true;
        } else {
            loggedIn = false;
            for (Iterator<StreamListener> it = listenersStream.iterator(); it.hasNext();) {
                it.next().onStreamFail(parser.getName() + ", failed authentication");
            }
            return;
        }

        msg = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + to.Host + "' version='1.0'>";
        writer.write(msg);
        writer.flush();
        restartParser();
        do {
            parser.next();
        } while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("features")));

        Iq bind = new Iq();
        bind.type = Iq.Type.set;
        ResourceBinding rb = new ResourceBinding();
        addChildParser(new ResourceBinding());
        addListener(to.Host, bind.id, this);

        if (from.Resource != null && from.Resource.length() > 0) {
            rb.resource = from.Resource;
        }
        bind.addChild(rb);
        writer.write(bind.toString());
        writer.flush();
    }

    protected void session() {
        try {
            String msg = "<iq type='set' id='sess'><session xmlns='" + XMLNS + "'/></iq>";
            writer.write(msg);
            writer.flush();
        } catch (final Exception ex) {
            System.err.println(ex);
            connectionFailed(ex.toString());
        }
    }

    @Override
    public boolean onIq(Iq iq) {
        if (iq.childs.isEmpty()) {
            return false;
        }
        String xmlns = ((StanzaChild) iq.childs.get(0)).getXMLNS();
        if (xmlns.equals(ResourceBinding.XMLNS)) {
            ResourceBinding rb = (ResourceBinding) iq.childs.get(0);
            if (rb.jid != null) {
                from.Resource = rb.jid.Resource;
            }
            for (Iterator<StreamListener> it = listenersStream.iterator(); it.hasNext();) {
                it.next().onStreamReady();
            }
            session();

            return true;
        }
        if (xmlns.equals(XMLNS)) {
            // no-op since rfc6120
            return true;
        }
        return false;
    }
}
