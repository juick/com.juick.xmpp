/*
 * Juick
 * Copyright (C) 2008-2013, ugnich
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
import com.juick.xmpp.StanzaChild;
import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * http://xmpp.org/extensions/xep-0066.html
 * @author ugnich
 */
public class XOOB implements StanzaChild {

    public final static String XMLNS = "jabber:x:oob";
    public final static String TagName = "x";
    public String URL = null;
    public String Desc = null;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public XOOB parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        XOOB oob = new XOOB();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            switch (tag) {
                case "url":
                    oob.URL = XmlUtils.getTagText(parser);
                    break;
                case "desc":
                    oob.Desc = XmlUtils.getTagText(parser);
                    break;
                default:
                    XmlUtils.skip(parser);
                    break;
            }
        }
        return oob;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(" xmlns='").append(XMLNS).append("'>");
        if (URL != null && !URL.isEmpty()) {
            str.append("<url>").append(StringEscapeUtils.escapeXml10(URL)).append("</url>");
        }
        if (Desc != null && !Desc.isEmpty()) {
            str.append("<desc>").append(StringEscapeUtils.escapeXml10(Desc)).append("</desc>");
        }
        str.append("</").append(TagName).append(">");
        return str.toString();
    }
}
