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
public class DiscoInfo implements ChildElement {

    public final static String XMLNS = "http://jabber.org/protocol/disco#info";
    public final static String TagName = "query";
    public Vector identities = new Vector();
    public Vector features = new Vector();

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public void addIdentity(final String category, final String type, final String name) {
        Identity i = new Identity();
        i.category = category;
        i.type = type;
        i.name = name;
        identities.addElement(i);
    }

    public void addFeature(final String feature) {
        features.addElement(feature);
    }

    public static DiscoInfo parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        DiscoInfo di = new DiscoInfo();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            if (tag.equals("identity")) {
                Identity i = new Identity();
                i.category = parser.getAttributeValue(null, "category");
                i.type = parser.getAttributeValue(null, "type");
                i.name = parser.getAttributeValue(null, "name");
                di.identities.addElement(i);
            } else if (tag.equals("feature")) {
                di.features.addElement(parser.getAttributeValue(null, "var"));
            } else {
                XmlUtils.skip(parser);
            }
        }
        return di;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + " xmlns='" + XMLNS + "'>";
        for (Enumeration e = identities.elements(); e.hasMoreElements();) {
            str += ((Identity) e.nextElement()).toString();
        }
        for (Enumeration e = features.elements(); e.hasMoreElements();) {
            str += "<feature var='" + e.nextElement() + "'/>";
        }
        str += "</" + TagName + ">";
        return str;
    }

    public static class Identity {

        public String category = null;
        public String type = null;
        public String name = null;

        @Override
        public String toString() {
            String str = "<identity";
            if (category != null) {
                str += " category='" + category + "'";
            }
            if (type != null) {
                str += " type='" + type + "'";
            }
            if (name != null) {
                str += " name='" + XmlUtils.escape(name) + "'";
            }
            str += "/>";
            return str;
        }
    }
}
