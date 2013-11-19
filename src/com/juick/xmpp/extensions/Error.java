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
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author ugnich
 */
public class Error implements StanzaChild {

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

    public Error() {
    }

    public Error(String type, String condition) {
        this.type = type;
        this.condition = condition;
    }

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public Error parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Error err = new Error();
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
        String str = "<" + TagName + "";
        if (by != null) {
            str += " by=\"" + XmlUtils.escape(by) + "\"";
        }
        if (type != null) {
            str += " type=\"" + XmlUtils.escape(type) + "\"";
        }

        if (condition != null) {
            str += ">";
            str += "<" + XmlUtils.escape(condition) + " xmlns=\"urn:ietf:params:xml:ns:xmpp-stanzas\"";
            if (text != null) {
                str += ">" + XmlUtils.escape(text) + "</" + XmlUtils.escape(condition) + ">";
            } else {
                str += "/>";
            }
            str += "</" + TagName + ">";
        } else {
            str += "/>";
        }

        return str;
    }
}
