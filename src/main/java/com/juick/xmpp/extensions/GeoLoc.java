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

import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * http://xmpp.org/extensions/xep-0080.html
 * @author Ugnich Anton
 */
public class GeoLoc implements StanzaChild {

    public final static String XMLNS = "http://jabber.org/protocol/geoloc";
    public final static String TagName = "geoloc";
    public String Description = null;
    public String Text = null;
    public String Lat = null;
    public String Lon = null;
    public int Accuracy = 0;
    public int JuickPlaceID = 0;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public GeoLoc parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        GeoLoc geoloc = new GeoLoc();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            switch (tag) {
                case "description":
                    geoloc.Description = XmlUtils.getTagText(parser);
                    break;
                case "text":
                    geoloc.Text = XmlUtils.getTagText(parser);
                    break;
                case "lat":
                    geoloc.Lat = XmlUtils.getTagText(parser);
                    break;
                case "lon":
                    geoloc.Lon = XmlUtils.getTagText(parser);
                    break;
                case "accuracy":
                    final String accuracyStr = XmlUtils.getTagText(parser);
                    geoloc.Accuracy = Integer.parseInt(accuracyStr);
                    break;
                case "uri":
                    final String uri = XmlUtils.getTagText(parser);
                    if (uri.startsWith("http://juick.com/places/")) {
                        geoloc.JuickPlaceID = Integer.parseInt(uri.substring(24));
                    }
                    break;
                default:
                    XmlUtils.skip(parser);
                    break;
            }
        }
        return geoloc;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(" xmlns='").append(XMLNS).append("'>");
        if (Lat != null && Lat.length() > 0) {
            str.append("<lat>").append(Lat).append("</lat>");
        }
        if (Lon != null && Lon.length() > 0) {
            str.append("<lon>").append(Lon).append("</lon>");
        }
        if (Accuracy > 0) {
            str.append("<accuracy>").append(Accuracy).append("</accuracy>");
        }
        if (Description != null && Description.length() > 0) {
            str.append("<description>").append(StringEscapeUtils.escapeXml10(Description)).append("</description>");
        }
        if (Text != null && Text.length() > 0) {
            str.append("<text>").append(StringEscapeUtils.escapeXml10(Text)).append("</text>");
        }
        if (JuickPlaceID > 0) {
            str.append("<uri>http://juick.com/places/").append(JuickPlaceID).append("</uri>");
        }
        str.append("</").append(TagName).append(">");
        return str.toString();
    }
}
