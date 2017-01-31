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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class DiscoInfo implements StanzaChild {

    public final static String XMLNS = "http://jabber.org/protocol/disco#info";
    public final static String TagName = "query";
    public ArrayList<Identity> identities = new ArrayList<Identity>();
    public ArrayList<String> features = new ArrayList<String>();

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public void addIdentity(final String category, final String type, final String name) {
        Identity i = new Identity();
        i.category = category;
        i.type = type;
        i.name = name;
        identities.add(i);
    }

    public void addFeature(final String feature) {
        features.add(feature);
    }

    @Override
    public DiscoInfo parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        DiscoInfo di = new DiscoInfo();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            switch (tag) {
                case "identity":
                    Identity i = new Identity();
                    i.category = parser.getAttributeValue(null, "category");
                    i.type = parser.getAttributeValue(null, "type");
                    i.name = parser.getAttributeValue(null, "name");
                    di.identities.add(i);
                    break;
                case "feature":
                    di.features.add(parser.getAttributeValue(null, "var"));
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
        String str = "<" + TagName + " xmlns='" + XMLNS + "'>";
        for (Identity identity : identities) {
            str += identity.toString();
        }
        for (String feature : features) {
            str += "<feature var='" + feature + "'/>";
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
