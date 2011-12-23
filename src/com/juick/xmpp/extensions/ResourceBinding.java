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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * http://xmpp.org/rfcs/rfc3920.html#bind
 * @author Ugnich Anton
 */
public class ResourceBinding implements ChildElement {

    public final static String XMLNS = "urn:ietf:params:xml:ns:xmpp-bind";
    public final static String TagName = "bind";
    public String resource;
    public JID jid;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public static ResourceBinding parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        ResourceBinding rb = new ResourceBinding();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            if (tag.equals("resource")) {
                rb.resource = XmlUtils.getTagText(parser);
            } else if (tag.equals("jid")) {
                rb.jid = new JID(XmlUtils.getTagText(parser));
            } else {
                XmlUtils.skip(parser);
            }
        }
        return rb;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + " xmlns='" + XMLNS + "'>";
        if (resource != null) {
            str += "<resource>" + XmlUtils.escape(resource) + "</lat>";
        }
        if (jid != null) {
            str += "<jid>" + jid.toString() + "</jid>";
        }
        str += "</" + TagName + ">";
        return str;
    }
}
