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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;

/**
 *
 * @author Ugnich Anton
 */
public class XConference implements StanzaChild {

    public final static String XMLNS = "jabber:x:conference";
    public final static String TagName = "x";
    public Jid jid;
    public String reason;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public XConference parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        XConference xc = new XConference();
        xc.jid = Jid.of(parser.getAttributeValue(null, "Jid"));
        xc.reason = parser.getAttributeValue(null, "reason");
        XmlUtils.skip(parser);
        return xc;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + " xmlns='" + XMLNS + "'";
        if (jid != null) {
            str += " jid=\"" + jid.toEscapedString() + "\"";
        }
        if (reason != null) {
            str += " reason=\"" + XmlUtils.escape(reason) + "\"";
        }
        str += "/>";
        return str;
    }
}
