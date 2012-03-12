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
import java.io.IOException;
import java.util.Enumeration;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class XmppConnectionClient extends XmppConnection implements IqListener {

    public final static String XMLNS = "urn:ietf:params:xml:ns:xmpp-session";

    public XmppConnectionClient(final JID jid, final String password, final String server, final int port, final boolean use_ssl) {
        super(jid, password, server, port, use_ssl);
    }

    @Override
    public void login() throws XmlPullParserException, IOException {
        String msg = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + jid.Host + "' version='1.0'>";
        writer.write(msg);
        writer.flush();
        parser.next(); // stream:stream

        XmppStreamFeatures features = XmppStreamFeatures.parse(parser);
        if (features.STARTTLS == XmppStreamFeatures.REQUIRED || features.PLAIN == XmppStreamFeatures.NOTAVAILABLE) {
            loggedIn = false;
            for (Enumeration e = listenersXmpp.elements(); e.hasMoreElements();) {
                XmppListener xl = (XmppListener) e.nextElement();
                xl.onAuthFailed("stream:features, failed authentication");
            }
            return;
        }

        msg = "<auth xmlns='urn:ietf:params:xml:ns:xmpp-sasl' mechanism='PLAIN'>";
        byte[] auth_msg = (jid.Bare() + '\0' + jid.Username + '\0' + password).getBytes();
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
            for (Enumeration e = listenersXmpp.elements(); e.hasMoreElements();) {
                XmppListener xl = (XmppListener) e.nextElement();
                xl.onAuthFailed(parser.getName() + ", failed authentication");
            }
            return;
        }

        msg = "<stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' to='" + jid.Host + "' version='1.0'>";
        writer.write(msg);
        writer.flush();
        restartParser();
        do {
            parser.next();
        } while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("features")));

        Iq bind = new Iq();
        bind.type = Iq.Type.set;
        ResourceBinding rb = new ResourceBinding();
        addListener(this.server, bind.id, this);

        if (jid.Resource != null && jid.Resource.length() > 0) {
            rb.resource = jid.Resource;
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
        String xmlns = ((ChildElement) iq.childs.get(0)).getXMLNS();
        if (xmlns.equals(ResourceBinding.XMLNS)) {
            ResourceBinding rb = (ResourceBinding) iq.childs.get(0);
            if (rb.jid != null) {
                jid.Resource = rb.jid.Resource;
            }
            for (Enumeration e = listenersXmpp.elements(); e.hasMoreElements();) {
                XmppListener xl = (XmppListener) e.nextElement();
                xl.onAuth(jid.Resource);
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

class XmppStreamFeatures {

    public static final int NOTAVAILABLE = -1;
    public static final int AVAILABLE = 0;
    public static final int REQUIRED = 1;
    public int STARTTLS = NOTAVAILABLE;
    public int ZLIB = NOTAVAILABLE;
    public int PLAIN = NOTAVAILABLE;
    public int DIGEST_MD5 = NOTAVAILABLE;
    public int X_GOOGLE_TOKEN = NOTAVAILABLE;
    public int REGISTER = NOTAVAILABLE;

    public static XmppStreamFeatures parse(final XmlPullParser parser) throws XmlPullParserException, IOException {
        XmppStreamFeatures features = new XmppStreamFeatures();

        parser.next(); // Go in stream:features
        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            final String xmlns = parser.getNamespace();
            if (tag.equals("starttls") && xmlns != null && xmlns.equals("urn:ietf:params:xml:ns:xmpp-tls")) {
                features.STARTTLS = AVAILABLE;
                while (parser.next() == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("required")) {
                        features.STARTTLS = REQUIRED;
                    } else {
                        XmlUtils.skip(parser);
                    }
                }
            } else if (tag.equals("compression") && xmlns != null && xmlns.equals("http://jabber.org/features/compress")) {
                while (parser.next() == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("method")) {
                        final String method = XmlUtils.getTagText(parser).toUpperCase();
                        if (method.equals("ZLIB")) {
                            features.ZLIB = AVAILABLE;
                        }
                    } else {
                        XmlUtils.skip(parser);
                    }
                }
            } else if (tag.equals("mechanisms") && xmlns != null && xmlns.equals("urn:ietf:params:xml:ns:xmpp-sasl")) {
                while (parser.next() == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("mechanism")) {
                        final String mechanism = XmlUtils.getTagText(parser).toUpperCase();
                        if (mechanism.equals("PLAIN")) {
                            features.PLAIN = AVAILABLE;
                        } else if (mechanism.equals("DIGEST-MD5")) {
                            features.DIGEST_MD5 = AVAILABLE;
                        } else if (mechanism.equals("X-GOOGLE-TOKEN")) {
                            features.X_GOOGLE_TOKEN = AVAILABLE;
                        }
                    } else {
                        XmlUtils.skip(parser);
                    }
                }
            } else if (tag.equals("register") && xmlns != null && xmlns.equals("http://jabber.org/features/iq-register")) {
                features.REGISTER = AVAILABLE;
                XmlUtils.skip(parser);
            } else {
                XmlUtils.skip(parser);
            }
        }
        return features;
    }
}
