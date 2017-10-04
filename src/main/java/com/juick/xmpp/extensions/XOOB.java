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
            if (tag.equals("url")) {
                oob.URL = XmlUtils.getTagText(parser);
            } else if (tag.equals("desc")) {
                oob.Desc = XmlUtils.getTagText(parser);
            } else {
                XmlUtils.skip(parser);
            }
        }
        return oob;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + " xmlns='" + XMLNS + "'>";
        if (URL != null && !URL.isEmpty()) {
            str += "<url>" + StringEscapeUtils.escapeXml10(URL) + "</url>";
        }
        if (Desc != null && !Desc.isEmpty()) {
            str += "<desc>" + StringEscapeUtils.escapeXml10(Desc) + "</desc>";
        }
        str += "</" + TagName + ">";
        return str;
    }
}
