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
package com.juick.xmpp.extensions;

import com.juick.xmpp.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class MucUser implements ChildElement {

    public final static String XMLNS = "http://jabber.org/protocol/muc#user";
    public final static String TagName = "x";
    public Item item = null;
    public Vector status = new Vector();
    public Invite invite = null;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public void setItem(final String affiliation, final String role) {
        item = new Item();
        item.affiliation = affiliation;
        item.role = role;
    }

    public void setInvite(final String from, final String reason) {
        invite = new Invite();
        invite.from = from;
        invite.reason = reason;
    }

    public void addStatus(final int code) {
        status.addElement(new Integer(code));
    }

    public static MucUser parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        MucUser di = new MucUser();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            if (tag.equals("item")) {
                di.item = new Item();
                di.item.affiliation = parser.getAttributeValue(null, "affiliation");
                di.item.role = parser.getAttributeValue(null, "role");
            } else if (tag.equals("status")) {
                String codestr = parser.getAttributeValue(null, "code");
                try {
                    int code = Integer.parseInt(codestr);
                    if (code >= 100 && code <= 999) {
                        di.status.addElement(new Integer(code));
                    }
                } catch (NumberFormatException e) {
                }
            } else {
                XmlUtils.skip(parser);
            }
        }
        return di;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + " xmlns='" + XMLNS + "'>";
        if (item != null) {
            str += item.toString();
        }
        if (invite != null) {
            str += invite.toString();
        }
        for (Enumeration e = status.elements(); e.hasMoreElements();) {
            str += "<status code='" + ((Integer) e.nextElement()) + "'/>";
        }
        str += "</" + TagName + ">";
        return str;
    }

    public static class Item {

        public static final class Affiliation {

            public static final String admin = "admin";
            public static final String member = "member";
            public static final String none = "none";
            public static final String outcast = "outcast";
            public static final String owner = "owner";
        }

        public static final class Role {

            public static final String moderator = "moderator";
            public static final String none = "none";
            public static final String participant = "participant";
            public static final String visitor = "visitor";
        }
        public String affiliation = null;
        public String role = null;

        @Override
        public String toString() {
            String str = "<item";
            if (affiliation != null) {
                str += " affiliation='" + affiliation + "'";
            }
            if (role != null) {
                str += " role='" + role + "'";
            }
            str += "/>";
            return str;
        }
    }

    public static class Invite {

        public String from = null;
        public String reason = null;

        @Override
        public String toString() {
            String str = "<invite";
            if (from != null) {
                str += " from='" + XmlUtils.escape(from) + "'";
            }
            str += ">";
            if (reason != null) {
                str += "<reason>" + XmlUtils.escape(reason) + "</reason>";
            }
            str += "</invite>";
            return str;
        }
    }
}
