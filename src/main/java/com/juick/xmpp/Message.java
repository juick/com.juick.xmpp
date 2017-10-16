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

import com.juick.xmpp.utils.XmlUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;

import java.text.ParseException;
import java.util.Map;

/**
 *
 * @author Ugnich Anton
 */
public class Message extends Stanza {

    public interface MessageListener {

        void onMessage(final Message message);
    }

    public static final class Type {

        public static final String chat = "chat";
        public static final String error = "error";
        public static final String groupchat = "groupchat";
        public static final String headline = "headline";
        public static final String normal = "normal";
    }
    public final static String TagName = "message";
    public String subject = null;
    public String body = null;
    public String thread = null;

    public Message() {
    }

    public Message(Jid to) {
        super(to);
    }

    public Message(Jid to, String type) {
        super(to, type);
    }

    public Message(Jid from, Jid to) {
        super(from, to);
    }

    public Message(Jid from, Jid to, String type) {
        super(from, to, type);
    }

    public static Message parse(XmlPullParser parser, Map<String, StanzaChild> childParsers) throws XmlPullParserException, java.io.IOException, ParseException {
        Message msg = new Message();
        msg.parseStanza(parser);

        String currentTag = parser.getName();
        while (!(parser.next() == XmlPullParser.END_TAG && parser.getName().equals(currentTag))) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                final String tag = parser.getName();
                final String xmlns = parser.getNamespace();
                if (tag.equals("subject")) {
                    msg.subject = XmlUtils.getTagText(parser);
                } else if (tag.equals("body")) {
                    msg.body = XmlUtils.getTagText(parser);
                } else if (tag.equals("thread")) {
                    msg.thread = XmlUtils.getTagText(parser);
                } else if (xmlns != null && childParsers != null) {
                    StanzaChild childparser = childParsers.get(xmlns);
                    if (childparser != null) {
                        StanzaChild child = childparser.parse(parser);
                        if (child != null) {
                            msg.addChild(child);
                        } else {
                            XmlUtils.skip(parser);
                        }
                    } else {
                        XmlUtils.skip(parser);
                    }
                } else {
                    XmlUtils.skip(parser);
                }
            }
        }
        return msg;
    }

    public Message reply() {
        Message reply = new Message();
        reply.from = to;
        reply.to = from;
        reply.type = type;
        reply.thread = thread;
        return reply;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(super.toString()).append(">");

        if (subject != null) {
            str.append("<subject>").append(StringEscapeUtils.escapeXml10(subject)).append("</subject>");
        }

        if (body != null) {
            str.append("<body>").append(StringEscapeUtils.escapeXml10(body)).append("</body>");
        }

        if (thread != null) {
            str.append("<thread>").append(StringEscapeUtils.escapeXml10(thread)).append("</thread>");
        }

        for (StanzaChild child : childs) {
            str.append(child.toString());
        }

        str.append("</").append(TagName).append(">");
        return str.toString();
    }
}
