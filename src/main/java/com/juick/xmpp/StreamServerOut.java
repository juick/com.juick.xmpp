/*
 * Copyright (C) 2008-2017, Juick
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
import com.juick.xmpp.helpers.StreamFeatures;
import com.juick.xmpp.util.DialbackUtils;
import com.juick.xmpp.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import rocks.xmpp.addr.Jid;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.juick.xmpp.StreamNamespaces.*;

/**
 * @author ugnich
 */
public class StreamServerOut extends Stream {
    private static final Logger logger = LoggerFactory.getLogger(StreamServerOut.class);
    private boolean secured = false;

    public boolean streamReady = false;
    String checkSID;
    private String dbKey;

    public StreamServerOut(Jid from, Jid to, InputStream is, OutputStream os, String checkSID, String dbKey) throws Exception {
        super(from, to, is, os);
        this.to = to;
        this.checkSID = checkSID;
        this.dbKey = dbKey;
        if (dbKey == null) {
            this.dbKey = DialbackUtils.generateDialbackKey(to, from, streamId);
        }
    }

    public void sendOpenStream() {
        send("<?xml version='1.0'?><stream:stream xmlns='jabber:server' id='" + streamId +
                "' xmlns:stream='http://etherx.jabber.org/streams' xmlns:db='jabber:server:dialback' from='" +
                from.toEscapedString()  + "' to='" + to.toEscapedString() + "' version='1.0'>");
    }

    void processDialback() {
        if (checkSID != null) {
            sendDialbackVerify(checkSID, dbKey);
        }
        send("<db:result from='" + from.toEscapedString() + "' to='" + to.toEscapedString() + "'>" +
                 dbKey + "</db:result>");
    }

    @Override
    public void handshake() {
        try {
            restartStream();

            sendOpenStream();

            parser.next(); // stream:stream
            streamId = parser.getAttributeValue(null, "id");
            if (streamId == null || streamId.isEmpty()) {
                throw new Exception("stream to " + to + " invalid first packet");
            }

            logger.debug("stream to {} {} open", to, streamId);
            boolean xmppversionnew = parser.getAttributeValue(null, "version") != null;
            if (!xmppversionnew) {
                processDialback();
            }

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                String tag = parser.getName();
                if (tag.equals("result") && parser.getNamespace().equals(NS_DB)) {
                    String type = parser.getAttributeValue(null, "type");
                    if (type != null && type.equals("valid")) {
                        streamReady = true;
                        streamHandler.ready();
                    } else {
                        logger.warn("stream to {} {} dialback fail", to, streamId);
                    }
                    XmlUtils.skip(parser);
                } else if (tag.equals("verify") && parser.getNamespace().equals(NS_DB)) {
                    String from = parser.getAttributeValue(null, "from");
                    String type = parser.getAttributeValue(null, "type");
                    String sid = parser.getAttributeValue(null, "id");
                    streamHandler.verify(from, type, sid);
                    XmlUtils.skip(parser);
                } else if (tag.equals("features") && parser.getNamespace().equals(NS_STREAM)) {
                    StreamFeatures features = StreamFeatures.parse(parser);
                    if (!secured && features.STARTTLS >= 0
                            && streamHandler.securing(this)) {
                        logger.debug("stream to {} {} securing", to.toEscapedString(), streamId);
                        send("<starttls xmlns=\"" + NS_TLS + "\" />");
                    } else {
                        processDialback();
                    }
                } else if (tag.equals("proceed") && parser.getNamespace().equals(NS_TLS)) {
                    streamHandler.proceed(this);
                } else if (secured && tag.equals("stream") && parser.getNamespace().equals(NS_STREAM)) {
                    streamId = parser.getAttributeValue(null, "id");
                } else if (tag.equals("error") && parser.getNamespace().equals(NS_XMPP_STREAMS)) {
                    StreamError streamError = StreamError.parse(parser);
                    streamHandler.dialbackError(this, streamError);
                } else {
                    String unhandledStanza = XmlUtils.parseToString(parser, false);
                    logger.warn("Unhandled stanza from {} {} : {}", to, streamId, unhandledStanza);
                }
            }
            streamHandler.finished(this, false);
        } catch (EOFException eofex) {
            streamHandler.finished(this, true);
        } catch (Exception e) {
            streamHandler.fail(e);
        }
    }

    public void sendDialbackVerify(String sid, String key) {
        send("<db:verify from='" + from.toEscapedString() + "' to='" + to + "' id='" + sid + "'>" +
                key + "</db:verify>");
    }

    public String getStreamID() {
        return streamId;
    }

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }
}
