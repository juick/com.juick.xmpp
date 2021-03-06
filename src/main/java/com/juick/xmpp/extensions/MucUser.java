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

import com.juick.xmpp.utils.XmlUtils;
import com.juick.xmpp.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;

/**
 *
 * @author Ugnich Anton
 */
public class MucUser implements StanzaChild {

    public final static String XMLNS = "http://jabber.org/protocol/muc#user";
    public final static String TagName = "x";
    public Item item = null;
    public ArrayList<Integer> status = new ArrayList<>();
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

    public void setInvite(final Jid from, final String reason) {
        invite = new Invite();
        invite.from = from;
        invite.reason = reason;
    }

    public void addStatus(final int code) {
        status.add(code);
    }

    @Override
    public MucUser parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        MucUser di = new MucUser();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            switch (tag) {
                case "item":
                    di.item = new Item();
                    di.item.affiliation = parser.getAttributeValue(null, "affiliation");
                    di.item.role = parser.getAttributeValue(null, "role");
                    break;
                case "status":
                    String codestr = parser.getAttributeValue(null, "code");
                    try {
                        int code = Integer.parseInt(codestr);
                        if (code >= 100 && code <= 999) {
                            di.status.add(code);
                        }
                    } catch (NumberFormatException e) {
                    }
                    break;
                default:
                    XmlUtils.skip(parser);
                    break;
            }
        }
        return di;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(" xmlns='").append(XMLNS).append("'>");
        if (item != null) {
            str.append(item.toString());
        }
        if (invite != null) {
            str.append(invite.toString());
        }
        for (Integer statu : status) {
            str.append("<status code='").append(statu).append("'/>");
        }
        str.append("</").append(TagName).append(">");
        return str.toString();
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
            StringBuilder str = new StringBuilder("<item");
            if (affiliation != null) {
                str.append(" affiliation='").append(affiliation).append("'");
            }
            if (role != null) {
                str.append(" role='").append(role).append("'");
            }
            str.append("/>");
            return str.toString();
        }
    }

    public static class Invite {

        public Jid from = null;
        public String reason = null;

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("<invite");
            if (from != null) {
                str.append(" from='").append(from.toEscapedString()).append("'");
            }
            str.append(">");
            if (reason != null) {
                str.append("<reason>").append(StringEscapeUtils.escapeXml10(reason)).append("</reason>");
            }
            str.append("</invite>");
            return str.toString();
        }
    }
}
