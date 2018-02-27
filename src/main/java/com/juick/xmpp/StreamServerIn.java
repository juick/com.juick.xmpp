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
import com.juick.xmpp.util.XmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.juick.xmpp.StreamNamespaces.*;


/**
 * @author ugnich
 */
public class StreamServerIn extends Stream {
    private static final Logger logger = LoggerFactory.getLogger(StreamServerIn.class);
    private final List<Jid> domains = new CopyOnWriteArrayList<>();

    public StreamServerIn(Jid from, InputStream is, OutputStream os) throws XmlPullParserException, IOException {
        super(from, null, is, os);
    }

    @Override
    public void handshake() {
        try {
            parser.next(); // stream:stream
            setUpdated();
            if (!parser.getName().equals("stream")
                    || !parser.getNamespace(null).equals(NS_SERVER)
                    || !parser.getNamespace("stream").equals(NS_STREAM)) {
                throw new Exception("invalid stream");
            }
            streamId = parser.getAttributeValue(null, "id");
            if (streamId == null) {
                streamId = UUID.randomUUID().toString();
            }
            boolean xmppversionnew = parser.getAttributeValue(null, "version") != null;
            String acceptfrom = parser.getAttributeValue(null, "from");

            if (streamHandler.accept(this, acceptfrom)) {
                sendOpenStream(acceptfrom, xmppversionnew);
            } else {
                throw new Exception(String.format("stream from %s is banned", acceptfrom));
            }

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.IGNORABLE_WHITESPACE) {
                    setUpdated();
                }
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                setUpdated();

                String tag = parser.getName();
                if (tag.equals("result") && parser.getNamespace().equals(NS_DB)) {
                    String dfrom = parser.getAttributeValue(null, "from");
                    String to = parser.getAttributeValue(null, "to");
                    logger.debug("stream from {} to {} {} asking for dialback", dfrom, to, streamId);
                    if (to != null && to.equals(from.toEscapedString())) {
                        String dbKey = XmlUtils.getTagText(parser);
                        setUpdated();
                        streamHandler.startDialback(this, Jid.of(dfrom), dbKey);
                    } else {
                        logger.warn("stream from " + dfrom + " " + streamId + " invalid to " + to);
                        break;
                    }
                } else if (tag.equals("verify") && parser.getNamespace().equals(NS_DB)) {
                    String vfrom = parser.getAttributeValue(null, "from");
                    String vto = parser.getAttributeValue(null, "to");
                    String vid = parser.getAttributeValue(null, "id");
                    String vkey = XmlUtils.getTagText(parser);
                    if (vfrom != null && vto != null && vid != null && vkey != null) {
                        streamHandler.verifyDialbackKey(vid, vfrom, vkey);
                    }
                } else if (!isSecured() && tag.equals("starttls") && parser.getNamespace().equals(NS_TLS)) {
                    streamHandler.starttls(this);
                } else if (isSecured() && tag.equals("stream") && parser.getNamespace().equals(NS_STREAM)) {
                    sendOpenStream(null, true);
                } else if (tag.equals("error") && parser.getNamespace().equals(NS_XMPP_STREAMS)) {
                    StreamError streamError = StreamError.parse(parser);
                    logger.warn("Stream error from {}: {}", streamId, streamError.getCondition());
                    streamHandler.finished(this, true);
                } else {
                    String unhandledStanza = XmlUtils.parseToString(parser, false);
                    logger.warn("Unhandled stanza from {}: {}", streamId, unhandledStanza);
                }
            }
            streamHandler.finished(this, false);
        } catch (EOFException ex) {
            streamHandler.finished(this, true);
        } catch (Exception e) {
            streamHandler.fail(e);
        }
    }

    private void sendOpenStream(String from, boolean xmppversionnew) {
        String openStream = "<?xml version='1.0'?><stream:stream xmlns='jabber:server' " +
                "xmlns:stream='http://etherx.jabber.org/streams' xmlns:db='jabber:server:dialback' from='" +
                this.from.toEscapedString() + "' id='" + streamId + "' version='1.0'>";
        if (xmppversionnew) {
            openStream += "<stream:features>";
            if (!isSecured() && !streamHandler.allowTls(this, from)) {
                openStream += "<starttls xmlns=\"" + NS_TLS + "\"><optional/></starttls>";
            }
            openStream += "</stream:features>";
        }
        send(openStream);
    }

    public void sendDialbackResult(Jid sfrom, String type) {
        send("<db:result from='" + from.toEscapedString() + "' to='" + sfrom + "' type='" + type + "'/>");
        if (type.equals("valid")) {
            domains.add(sfrom);
            logger.debug("stream from {} {} ready", sfrom, streamId);
        }
    }

    boolean checkFromTo(XmlPullParser parser) {
        String cfrom = parser.getAttributeValue(null, "from");
        String cto = parser.getAttributeValue(null, "to");
        if (StringUtils.isNotEmpty(cfrom) &&  StringUtils.isNotEmpty(cto)) {
            Jid jidto = Jid.of(cto);
            if (jidto.getDomain().equals(from.getDomain())) {
                Jid jidfrom = Jid.of(cfrom);
                for (Jid aFrom : domains) {
                    if (aFrom.equals(Jid.of(jidfrom.getDomain()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
