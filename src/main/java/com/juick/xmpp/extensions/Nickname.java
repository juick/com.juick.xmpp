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

import org.apache.commons.text.StringEscapeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * http://xmpp.org/extensions/xep-0172.html
 * @author ugnich
 */
public class Nickname implements StanzaChild {

    public final static String XMLNS = "http://jabber.org/protocol/nick";
    public final static String TagName = "nick";
    public String Nickname = null;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public Nickname parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Nickname nick = new Nickname();

        nick.Nickname = XmlUtils.getTagText(parser);

        if (nick.Nickname != null && !nick.Nickname.isEmpty()) {
            return nick;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        String str = "<" + TagName + " xmlns='" + XMLNS + "'>";
        str += StringEscapeUtils.escapeXml10(Nickname);
        str += "</" + TagName + ">";
        return str;
    }
}
