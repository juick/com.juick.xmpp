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
public class DiscoItems implements ChildElement {

    public final static String XMLNS = "http://jabber.org/protocol/disco#items";
    public final static String TagName = "query";
    public Vector items = new Vector();

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public void addItem(final JID jid, final String name) {
        Item i = new Item();
        i.jid = jid;
        i.name = name;
        items.addElement(i);
    }

    public static DiscoItems parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        DiscoItems di = new DiscoItems();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            if (tag.equals("item")) {
                Item i = new Item();
                final String strJID = parser.getAttributeValue(null, "jid");
                if (strJID != null) {
                    i.jid = new JID(strJID);
                }
                i.name = parser.getAttributeValue(null, "name");
                di.items.addElement(i);
            } else {
                XmlUtils.skip(parser);
            }
        }
        return di;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + " xmlns='" + XMLNS + "'>";
        for (Enumeration e = items.elements(); e.hasMoreElements();) {
            str += ((Item) e.nextElement()).toString();
        }
        str += "</" + TagName + ">";
        return str;
    }

    public static class Item {

        public JID jid = null;
        public String name = null;

        @Override
        public String toString() {
            String str = "<item";
            if (jid != null) {
                //TODO нужен escape?
                str += " jid='" + jid.toString() + "'";
            }
            if (name != null) {
                str += " name='" + XmlUtils.escape(name) + "'";
            }
            str += "/>";
            return str;
        }
    }
}
