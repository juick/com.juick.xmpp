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

import com.juick.xmpp.extensions.StreamError;
import com.juick.xmpp.utils.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import rocks.xmpp.addr.Jid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Ugnich Anton
 */
public abstract class Stream {

    public static final String NS_STREAM = "http://etherx.jabber.org/streams";

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Jid from;
    public Jid to;
    private InputStream is;
    private OutputStream os;
    protected XmlPullParserFactory factory;
    protected XmlPullParser parser;
    protected OutputStreamWriter writer;
    Map<String, StanzaChild> childParsers = new HashMap<>();
    List<StreamListener> listenersStream = new ArrayList<>();
    List<Message.MessageListener> listenersMessage = new ArrayList<>();
    List<Presence.PresenceListener> listenersPresence = new ArrayList<>();
    List<Iq.IqListener> listenersIq = new ArrayList<>();
    HashMap<String, Iq.IqListener> listenersIqId = new HashMap<>();
    private boolean loggedIn;
    private Instant created;
    private Instant updated;
    private String streamId;

    public Stream(final Jid from, final Jid to, final InputStream is, final OutputStream os) throws XmlPullParserException {
        this.from = from;
        this.to = to;
        this.is = is;
        this.os = os;
        factory = XmlPullParserFactory.newInstance();
        created = updated = Instant.now();
        streamId = UUID.randomUUID().toString();
    }

    public void restartStream() throws XmlPullParserException, IOException {
        parser = factory.newPullParser();
        parser.setInput(new InputStreamReader(is, StandardCharsets.UTF_8));
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
    }

    public void connect() {
        try {
            restartStream();
            handshake();
            parse();
        } catch (final Exception e) {
            connectionFailed(e);
        }
    }

    public void addChildParser(StanzaChild childparser) {
        childParsers.put(childparser.getXMLNS(), childparser);
    }

    public void removeChildParser(final String xmlns) {
        childParsers.remove(xmlns);
    }

    public void addListener(final StreamListener l) {
        if (!listenersStream.contains(l)) {
            listenersStream.add(l);
        }
    }

    public void addListener(final Message.MessageListener l) {
        if (!listenersMessage.contains(l)) {
            listenersMessage.add(l);
        }
    }

    public void addListener(final Presence.PresenceListener l) {
        if (!listenersPresence.contains(l)) {
            listenersPresence.add(l);
        }
    }

    public void addListener(final Iq.IqListener l) {
        if (!listenersIq.contains(l)) {
            listenersIq.add(l);
        }
    }

    public void addListener(final String Jid, final String id, final Iq.IqListener iql) {
        listenersIqId.put(Jid + "\n" + id, iql);
    }

    public boolean removeListener(final StreamListener l) {
        return listenersStream.remove(l);
    }

    public boolean removeListener(final Message.MessageListener l) {
        return listenersMessage.remove(l);
    }

    public boolean removeListener(final Presence.PresenceListener l) {
        return listenersPresence.remove(l);
    }

    public boolean removeListener(final Iq.IqListener l) {
        return listenersIq.remove(l);
    }

    public abstract void handshake() throws XmlPullParserException, IOException;

    public void logoff() {
        setLoggedIn(false);
        try {
            writer.flush();
            writer.close();
            //TODO close parser
        } catch (final Exception e) {
            connectionFailed(e);
        }
    }

    public void send(final Stanza s) {
        send(s.toString());
    }

    public void send(final String str) {
        try {
            updated = Instant.now();
            writer.write(str);
            writer.flush();
        } catch (final Exception e) {
            connectionFailed(e);
        }
    }

    private void parse() throws IOException, ParseException {
        try {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.IGNORABLE_WHITESPACE) {
                    updated = Instant.now();
                }
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                updated = Instant.now();
                final String tag = parser.getName();
                switch (tag) {
                    case "message":
                        Message msg = Message.parse(parser, childParsers);
                        for (Message.MessageListener listener : listenersMessage) {
                            listener.onMessage(msg);
                        }
                        break;
                    case "presence":
                        Presence p = Presence.parse(parser, childParsers);
                        for (Presence.PresenceListener listener : listenersPresence) {
                            listener.onPresence(p);
                        }
                        break;
                    case "iq":
                        Iq iq = Iq.parse(parser, childParsers);
                        final String key = (iq.from == null) ? "" : iq.from.toEscapedString() + "\n" + iq.id;
                        boolean parsed = false;
                        if (listenersIqId.containsKey(key)) {
                            Iq.IqListener l = listenersIqId.get(key);
                            parsed = l.onIq(iq);
                            listenersIqId.remove(key);
                        } else {
                            for (Iq.IqListener listener : listenersIq) {
                                parsed |= listener.onIq(iq);
                            }
                        }
                        if (!parsed) {
                            send(iq.error());
                        }
                        break;
                    case "error":
                        StreamError error = StreamError.parse(parser);
                        connectionFailed(new Exception(error.getCondition()));
                        return;
                    default:
                        XmlUtils.skip(parser);
                        break;
                }
            }
        } catch (XmlPullParserException e) {
            StreamError invalidXmlError = new StreamError("invalid-xml");
            send(invalidXmlError.toString());
            connectionFailed(new Exception(invalidXmlError.getCondition()));
        }

    }

    /**
     * This method is used to be called on a parser or a connection error.
     * It tries to close the XML-Reader and XML-Writer one last time.
     */
    protected void connectionFailed(final Exception ex) {
        if (isLoggedIn()) {
            try {
                writer.close();
                //TODO close parser
            } catch (Exception e) {
            }
        }

        for (StreamListener listener : listenersStream) {
            listener.fail(ex);
        }
    }

    public void setInputStream(InputStream is) {
        this.is = is;
    }
    public void setOutputStream(OutputStream os) {
        this.os = os;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getUpdated() {
        return updated;
    }
    public String getStreamId() {
        return streamId;
    }
}
