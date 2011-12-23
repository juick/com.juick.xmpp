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
package com.juick.xmpp;

import com.juick.xmpp.extensions.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class Stanza {

    public JID from = null;
    public JID to = null;
    public String id = Long.toString(System.currentTimeMillis());
    public String type = null;
    public Vector childs = new Vector();

    public void addChild(ChildElement child) {
        childs.addElement(child);
    }

    public boolean hasChilds() {
        return !childs.isEmpty();
    }

    public ChildElement getChild(String XMLNS) {
        for (Enumeration e = childs.elements(); e.hasMoreElements();) {
            ChildElement ce = (ChildElement) e.nextElement();
            if (ce.getXMLNS().equals(XMLNS)) {
                return ce;
            }
        }
        return null;
    }

    public void parseStanza(XmlPullParser parser) {
        final String fromStr = parser.getAttributeValue(null, "from");
        if (fromStr != null) {
            from = new JID(fromStr);
        }

        final String toStr = parser.getAttributeValue(null, "to");
        if (toStr != null) {
            to = new JID(toStr);
        }

        id = parser.getAttributeValue(null, "id");
        type = parser.getAttributeValue(null, "type");
    }

    public static ChildElement parseChild(final String xmlns, XmlPullParser parser) throws XmlPullParserException, IOException {
        if (xmlns.equals(ResourceBinding.XMLNS)) {
            return ResourceBinding.parse(parser);
        } else if (xmlns.equals(Roster.XMLNS)) {
            return Roster.parse(parser);
        } else if (xmlns.equals(DiscoInfo.XMLNS)) {
            return DiscoInfo.parse(parser);
        } else if (xmlns.equals(DiscoItems.XMLNS)) {
            return DiscoItems.parse(parser);
        } else if (xmlns.equals(JuickMessage.XMLNS)) {
            return JuickMessage.parse(parser);
        } else if (xmlns.equals(JuickUser.XMLNS)) {
            return JuickUser.parse(parser);
        } else if (xmlns.equals(Games.XMLNS)) {
            return Games.parse(parser);
        } else if (xmlns.equals(GameTicTacToe.XMLNS)) {
            return GameTicTacToe.parse(parser);
        } else if (xmlns.equals(GeoLoc.XMLNS)) {
            return GeoLoc.parse(parser);
        } else if (xmlns.equals(Mood.XMLNS)) {
            return Mood.parse(parser);
        } else if (xmlns.equals(MucUser.XMLNS)) {
            return MucUser.parse(parser);
        } else if (xmlns.equals(Delay.XMLNS)) {
            return Delay.parse(parser);
        }
        return null;
    }

    @Override
    public String toString() {
        String str = new String();
        if (from != null) {
            str += " from='" + from.toString() + "'";
        }
        if (to != null) {
            str += " to='" + to.toString() + "'";
        }
        if (id != null) {
            str += " id='" + XmlUtils.escape(id) + "'";
        }
        if (type != null) {
            str += " type='" + XmlUtils.escape(type) + "'";
        }
        return str;
    }
}
