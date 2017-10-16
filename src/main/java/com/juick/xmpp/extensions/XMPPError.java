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

import com.juick.xmpp.StanzaChild;
import com.juick.xmpp.utils.XmlUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 *
 * @author ugnich
 */
public class XMPPError implements StanzaChild {

    public static final class Type {

        public static final String auth = "auth";
        public static final String cancel = "cancel";
        public static final String continue_ = "continue";
        public static final String modify = "modify";
        public static final String wait = "wait";
    }
    public final static String XMLNS = "";
    public final static String TagName = "error";
    public String by = null;
    public String type = null;
    public String condition = null;
    public String text = null;

    public XMPPError() {
    }

    public XMPPError(String type, String condition) {
        this.type = type;
        this.condition = condition;
    }

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public XMPPError parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        XMPPError err = new XMPPError();
        err.by = parser.getAttributeValue(null, "by");
        err.type = parser.getAttributeValue(null, "type");

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            final String xmlns = parser.getNamespace();
            if (xmlns.equals("urn:ietf:params:xml:ns:xmpp-stanzas")) {
                err.condition = tag;
                err.text = XmlUtils.getTagText(parser);
            } else {
                XmlUtils.skip(parser);
            }
        }

        return err;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append("");
        if (by != null) {
            str.append(" by=\"").append(StringEscapeUtils.escapeXml10(by)).append("\"");
        }
        if (type != null) {
            str.append(" type=\"").append(StringEscapeUtils.escapeXml10(type)).append("\"");
        }

        if (condition != null) {
            str.append(">");
            str.append("<").append(StringEscapeUtils.escapeXml10(condition)).append(" xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"");
            if (text != null) {
                str.append(">").append(StringEscapeUtils.escapeXml10(text)).append("</").append(StringEscapeUtils.escapeXml10(condition))
                    .append(">");
            } else {
                str.append("/>");
            }
            str.append("</").append(TagName).append(">");
        } else {
            str.append("/>");
        }

        return str.toString();
    }
}
