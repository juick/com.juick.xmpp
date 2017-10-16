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
package com.juick.xmpp.utils;

import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class XmlUtils {

    public static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        String tag = parser.getName();
        while (parser.getName() != null && !(parser.next() == XmlPullParser.END_TAG && parser.getName().equals(tag))) {
        }
    }

    public static String getTagText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String ret = "";
        String tag = parser.getName();

        if (parser.next() == XmlPullParser.TEXT) {
            ret = parser.getText();
        }

        while (!(parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals(tag))) {
            parser.next();
        }

        return ret;
    }

    public static String parseToString(XmlPullParser parser, boolean skipXMLNS) throws XmlPullParserException, IOException {
        String tag = parser.getName();
        StringBuilder ret = new StringBuilder("<").append(tag);

        // skipXMLNS for xmlns="jabber:client"

        String ns = parser.getNamespace();
        if (!skipXMLNS && ns != null && !ns.isEmpty()) {
            ret.append(" xmlns=\"").append(ns).append("\"");
        }

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attr = parser.getAttributeName(i);
            if ((!skipXMLNS || !attr.equals("xmlns")) && !attr.contains(":")) {
                ret.append(" ").append(attr).append("=\"").append(StringEscapeUtils.escapeXml10(parser.getAttributeValue(i))).append("\"");
            }
        }
        ret.append(">");

        while (!(parser.next() == XmlPullParser.END_TAG && parser.getName().equals(tag))) {
            int event = parser.getEventType();
            if (event == XmlPullParser.START_TAG) {
                if (!parser.getName().contains(":")) {
                    ret.append(parseToString(parser, false));
                } else {
                    skip(parser);
                }
            } else if (event == XmlPullParser.TEXT) {
                ret.append(StringEscapeUtils.escapeXml10(parser.getText()));
            }
        }

        ret.append("</").append(tag).append(">");
        return ret.toString();
    }
}
