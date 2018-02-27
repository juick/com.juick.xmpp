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

import com.juick.xmpp.helpers.StreamError;
import com.juick.xmpp.util.XmlUtils;
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
import java.time.Instant;
import java.util.UUID;

/**
 *
 * @author Ugnich Anton
 */
public abstract class Stream {

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    Jid from;
    public Jid to;
    private InputStream is;
    private OutputStream os;
    private XmlPullParserFactory factory;
    protected XmlPullParser parser;
    private OutputStreamWriter writer;
    StreamHandler streamHandler;
    private boolean loggedIn;
    private Instant created;
    private Instant updated;
    String streamId;
    private boolean secured;

    public Stream(final Jid from, final Jid to, final InputStream is, final OutputStream os) throws XmlPullParserException {
        this.from = from;
        this.to = to;
        this.is = is;
        this.os = os;
        factory = XmlPullParserFactory.newInstance();
        created = updated = Instant.now();
        streamId = UUID.randomUUID().toString();
    }

    void restartStream() throws XmlPullParserException {
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
        } catch (XmlPullParserException e) {
            StreamError invalidXmlError = new StreamError("invalid-xml");
            send(invalidXmlError.toString());
            connectionFailed(new Exception(invalidXmlError.getCondition()));
        } catch (IOException e) {
            connectionFailed(e);
        }
    }

    public void setHandler(final StreamHandler streamHandler) {
        this.streamHandler = streamHandler;
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

    public void send(final String str) {
        try {
            updated = Instant.now();
            writer.write(str);
            writer.flush();
        } catch (final Exception e) {
            connectionFailed(e);
        }
    }

    private void parse() throws IOException, XmlPullParserException {
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.IGNORABLE_WHITESPACE) {
                setUpdated();
            }
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            setUpdated();
            final String tag = parser.getName();
            switch (tag) {
                case "message":
                case "presence":
                case "iq":
                    streamHandler.stanzaReceived(XmlUtils.parseToString(parser, false));
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
    }

    /**
     * This method is used to be called on a parser or a connection error.
     * It tries to close the XML-Reader and XML-Writer one last time.
     */
    private void connectionFailed(final Exception ex) {
        if (isLoggedIn()) {
            try {
                writer.close();
                //TODO close parser
            } catch (Exception e) {
            }
        }
        streamHandler.fail(ex);
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

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public void setUpdated() {
        this.updated = Instant.now();
    }
}
