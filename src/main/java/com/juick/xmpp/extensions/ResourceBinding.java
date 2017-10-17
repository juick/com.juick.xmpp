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
 * http://xmpp.org/rfcs/rfc3920.html#bind
 * @author Ugnich Anton
 */
public class ResourceBinding implements StanzaChild {

    public final static String XMLNS = "urn:ietf:params:xml:ns:xmpp-bind";
    public final static String TagName = "bind";
    public String resource;
    public Jid jid;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public ResourceBinding parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        ResourceBinding rb = new ResourceBinding();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            switch (tag) {
                case "resource":
                    rb.resource = XmlUtils.getTagText(parser);
                    break;
                case "Jid":
                    rb.jid = Jid.of(XmlUtils.getTagText(parser));
                    break;
                default:
                    XmlUtils.skip(parser);
                    break;
            }
        }
        return rb;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(" xmlns='").append(XMLNS).append("'>");
        if (resource != null) {
            str.append("<resource>").append(StringEscapeUtils.escapeXml10(resource)).append("</lat>");
        }
        if (jid != null) {
            str.append("<Jid>").append(jid.toEscapedString()).append("</Jid>");
        }
        str.append("</").append(TagName).append(">");
        return str.toString();
    }
}
