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
import rocks.xmpp.addr.Jid;

/**
 *
 * @author Ugnich Anton
 */
public class Delay implements StanzaChild {

    public final static String XMLNS = "urn:xmpp:delay";
    public final static String TagName = "delay";
    public Jid from = null;
    public String stamp = null;
    public String description = null;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public Delay parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Delay delay = new Delay();

        String from = parser.getAttributeValue(null, "from");
        if (from != null) {
            delay.from = Jid.of(from);
        }
        delay.stamp = parser.getAttributeValue(null, "stamp");
        delay.description = XmlUtils.getTagText(parser);

        return delay;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(" xmlns='").append(XMLNS).append("'");
        if (from != null) {
            str.append(" from='").append(from.toEscapedString()).append("'");
        }
        if (stamp != null) {
            str.append(" stamp='").append(stamp).append("'");
        }
        str.append(">");

        if (description != null) {
            str.append(StringEscapeUtils.escapeXml10(description));
        }

        str.append("</").append(TagName).append(">");
        return str.toString();
    }
}
