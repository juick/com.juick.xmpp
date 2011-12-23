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

import java.util.Enumeration;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class Presence extends Stanza {

    public static final class Type {

        public static final String unavailable = "unavailable";
        public static final String subscribe = "subscribe";
        public static final String subscribed = "subscribed";
        public static final String unsubscribe = "unsubscribe";
        public static final String unsubscribed = "unsubscribed";
        public static final String probe = "probe";
        public static final String error = "error";
    }

    public static final class Show {

        public static final String away = "away";
        public static final String chat = "chat";
        public static final String dnd = "dnd";
        public static final String xa = "xa";
    }
    public final static String TagName = "presence";
    public int priority = -255; // The value MUST be an integer between -128 and +127 - RFC 3921
    public String show = null; // away / chat / dnd / xa
    public String status = null;

    public static Presence parse(XmlPullParser parser) throws XmlPullParserException, java.io.IOException {
        Presence p = new Presence();
        p.parseStanza(parser);

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            final String xmlns = parser.getNamespace();
            if (tag.equals("status")) {
                p.status = XmlUtils.getTagText(parser);
            } else if (tag.equals("show")) {
                p.show = XmlUtils.getTagText(parser);
            } else if (tag.equals("priority")) {
                String priority = XmlUtils.getTagText(parser);
                if (priority.length() > 0) {
                    try {
                        p.priority = Integer.parseInt(priority);
                    } catch (NumberFormatException e) {
                    }
                }
            } else if (xmlns != null) {
                ChildElement child = Stanza.parseChild(xmlns, parser);
                if (child != null) {
                    p.addChild(child);
                } else {
                    XmlUtils.skip(parser);
                }
            } else {
                XmlUtils.skip(parser);
            }
        }
        return p;
    }

    public Presence reply() {
        Presence reply = new Presence();
        reply.from = to;
        reply.to = from;
        return reply;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + super.toString() + ">";

        if (show != null) {
            str += "<show>" + XmlUtils.escape(show) + "</show>";
        }

        if (priority >= -128 && priority <= 127) {
            str += "<priority>" + priority + "</priority>";
        }

        if (status != null) {
            str += "<status>" + XmlUtils.escape(status) + "</status>";
        }

        for (Enumeration e = childs.elements(); e.hasMoreElements();) {
            str += e.nextElement().toString();
        }

        str += "</" + TagName + ">";
        return str;
    }
}
