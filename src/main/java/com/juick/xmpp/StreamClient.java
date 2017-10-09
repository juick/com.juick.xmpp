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

import com.juick.xmpp.extensions.ResourceBinding;
import com.juick.xmpp.extensions.StreamFeatures;
import org.apache.commons.codec.binary.Base64;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

/**
 *
 * @author Ugnich Anton
 */
public class StreamClient extends Stream implements Iq.IqListener {

    public final static String XMLNS = "urn:ietf:params:xml:ns:xmpp-session";
    String password;

    public StreamClient(Jid from, Jid to, InputStream is, OutputStream os, String password) throws XmlPullParserException {
        super(from, to, is, os);
        this.password = password;
    }

    @Override
    public void handshake() throws XmlPullParserException, IOException {
        String msg = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + to.getDomain() + "' version='1.0'>";
        writer.write(msg);
        writer.flush();
        parser.next(); // stream:stream

        StreamFeatures features = StreamFeatures.parse(parser);
        if (features.STARTTLS == StreamFeatures.REQUIRED || features.PLAIN == StreamFeatures.NOTAVAILABLE) {
            setLoggedIn(false);
            for (Iterator<StreamListener> it = listenersStream.iterator(); it.hasNext();) {
                it.next().fail(new IOException("stream:features, failed authentication"));
            }
            return;
        }

        msg = "<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>";
        byte[] auth_msg = (from.asBareJid().toEscapedString() + '\0' + from.getLocal() + '\0' + password).getBytes();
        msg = msg + Base64.encodeBase64String(auth_msg) + "</auth>";
        writer.write(msg);
        writer.flush();
        parser.next();
        if (parser.getName().equals("success")) {
            do {
                parser.next();
            } while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("success")));
            setLoggedIn(true);
        } else {
            setLoggedIn(false);
            for (StreamListener listener : listenersStream) {
                listener.fail(new IOException(String.format("%s, failed authentication", parser.getName())));
            }
            return;
        }

        send("<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + to.getDomain() +
                "' version='1.0'>");
        restartStream();
        do {
            parser.next();
        } while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("features")));

        Iq bind = new Iq();
        bind.type = Iq.Type.set;
        ResourceBinding rb = new ResourceBinding();
        addChildParser(new ResourceBinding());
        addListener(to.getDomain(), bind.id, this);

        if (from.getResource() != null && from.getResource().length() > 0) {
            rb.resource = from.getResource();
        }
        bind.addChild(rb);
        writer.write(bind.toString());
        writer.flush();
    }

    protected void session() {
        try {
            send("<iq type='set' id='sess'><session xmlns='" + XMLNS + "'/></iq>");
        } catch (final Exception ex) {
            connectionFailed(ex);
        }
    }

    @Override
    public boolean onIq(Iq iq) {
        if (iq.childs.isEmpty()) return false;
        String xmlns = iq.childs.get(0).getXMLNS();
        if (xmlns.equals(ResourceBinding.XMLNS)) {
            ResourceBinding rb = (ResourceBinding) iq.childs.get(0);
            if (rb.jid != null) {
                from = from.withResource(rb.jid.getResource());
            }
            listenersStream.forEach(StreamListener::ready);
            session();

            return true;
        }
        return xmlns.equals(XMLNS);
    }
}
