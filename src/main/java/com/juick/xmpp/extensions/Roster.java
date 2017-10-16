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

import com.juick.xmpp.StanzaChild;
import com.juick.xmpp.utils.XmlUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;

import java.io.IOException;
import java.util.ArrayList;

/**
 * http://xmpp.org/rfcs/rfc3921.html#roster
 * @author Ugnich Anton
 */
public class Roster implements StanzaChild {

    public final static String XMLNS = "jabber:iq:roster";
    public final static String TagName = "query";
    public ArrayList<Item> items = new ArrayList<>();

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public Roster parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Roster roster = new Roster();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            if (tag.equals("item")) {
                Item ri = new Item();
                final String strJid = parser.getAttributeValue(null, "Jid");
                if (strJid != null) {
                    ri.jid = Jid.of(strJid);
                }
                ri.name = parser.getAttributeValue(null, "name");
                ri.subscription = parser.getAttributeValue(null, "subscription");
                while (parser.next() == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("group")) {
                        ri.group = XmlUtils.getTagText(parser);
                    } else {
                        XmlUtils.skip(parser);
                    }
                }
                roster.items.add(ri);
            } else {
                XmlUtils.skip(parser);
            }
        }
        return roster;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(" xmlns='").append(XMLNS).append("'>");
        for (Item item : items) {
            str.append(item.toString());
        }
        str.append("</").append(TagName).append(">");
        return str.toString();
    }

    public static class Item {

        public Jid jid = null;
        public String name = null;
        public String subscription = null;
        public String group = null;

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("<item");
            if (jid != null) {
                str.append(" jid='").append(jid.toEscapedString()).append("'");
            }
            if (name != null) {
                str.append(" name='").append(StringEscapeUtils.escapeXml10(name)).append("'");
            }
            if (subscription != null) {
                str.append(" subscription='").append(subscription).append("'");
            }
            str.append(">");
            if (group != null) {
                str.append("<group>").append(StringEscapeUtils.escapeXml10(group)).append("</group>");
            }
            str.append("</item>");
            return str.toString();
        }
    }
}
