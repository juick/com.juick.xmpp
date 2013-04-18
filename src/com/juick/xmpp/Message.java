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
import java.util.HashMap;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class Message extends Stanza {

    public static final class Type {

        public static final String chat = "chat";
        public static final String error = "result";
        public static final String groupchat = "groupchat";
        public static final String headline = "headline";
        public static final String normal = "normal";
    }
    public final static String TagName = "message";
    public String subject = null;
    public String body = null;
    public String thread = null;

    public static Message parse(XmlPullParser parser, HashMap<String, StanzaChild> childParsers) throws XmlPullParserException, java.io.IOException {
        Message msg = new Message();
        msg.parseStanza(parser);

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            final String xmlns = parser.getNamespace();
            if (tag.equals("subject")) {
                msg.subject = XmlUtils.getTagText(parser);
            } else if (tag.equals("body")) {
                msg.body = XmlUtils.getTagText(parser);
            } else if (tag.equals("thread")) {
                msg.thread = XmlUtils.getTagText(parser);
            } else if (xmlns != null) {
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
        String str = "<" + TagName + super.toString() + ">";

        if (subject != null) {
            str += "<subject>" + XmlUtils.escape(subject) + "</subject>";
        }

        if (body != null) {
            str += "<body>" + XmlUtils.escape(body) + "</body>";
        }

        if (thread != null) {
            str += "<thread>" + XmlUtils.escape(thread) + "</thread>";
        }

        Iterator<StanzaChild> i = childs.iterator();
        while (i.hasNext()) {
            str += i.next().toString();
        }

        str += "</" + TagName + ">";
        return str;
    }
}
