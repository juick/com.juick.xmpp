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

import com.juick.xmpp.utils.XmlUtils;
import java.util.ArrayList;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import rocks.xmpp.addr.Jid;

/**
 *
 * @author Ugnich Anton
 */
public class Stanza {

    public Jid from = null;
    public Jid to = null;
    public String id = Long.toString(System.currentTimeMillis());
    public String type = null;
    public ArrayList<StanzaChild> childs = new ArrayList<StanzaChild>();

    public Stanza() {
    }

    public Stanza(Jid to) {
        this.to = to;
    }

    public Stanza(Jid to, String type) {
        this.to = to;
        this.type = type;
    }

    public Stanza(Jid from, Jid to) {
        this.from = from;
        this.to = to;
    }

    public Stanza(Jid from, Jid to, String type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public void addChild(StanzaChild child) {
        childs.add(child);
    }

    public boolean hasChilds() {
        return !childs.isEmpty();
    }

    public StanzaChild getChild(String XMLNS) {
        Iterator<StanzaChild> i = childs.iterator();
        while (i.hasNext()) {
            StanzaChild ce = (StanzaChild) i.next();
            if (ce.getXMLNS().equals(XMLNS)) {
                return ce;
            }
        }
        return null;
    }

    public void parseStanza(XmlPullParser parser) {
        final String fromStr = parser.getAttributeValue(null, "from");
        if (fromStr != null) {
            from = Jid.of(fromStr);
        }

        final String toStr = parser.getAttributeValue(null, "to");
        if (toStr != null) {
            to = Jid.of(toStr);
        }

        id = parser.getAttributeValue(null, "id");
        type = parser.getAttributeValue(null, "type");
    }

    @Override
    public String toString() {
        String str = "";
        if (from != null) {
            str += " from='" + from.toEscapedString() + "'";
        }
        if (to != null) {
            str += " to='" + to.toEscapedString() + "'";
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
