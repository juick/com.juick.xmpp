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

import java.util.Enumeration;
import org.xmlpull.v1.*;

/**
 *
 * @author Ugnich Anton
 */
public class Iq extends Stanza {

    public static final class Type {

        public static final String get = "get";
        public static final String set = "set";
        public static final String result = "result";
        public static final String error = "error";
    }
    public final static String TagName = "iq";

    @Override
    public void addChild(ChildElement child) {
        childs.removeAllElements();
        childs.addElement(child);
    }

    public ChildElement getChild() {
        if (!childs.isEmpty()) {
            return (ChildElement) childs.firstElement();
        } else {
            return null;
        }
    }

    public Iq reply() {
        Iq reply = new Iq();
        reply.from = to;
        reply.to = from;
        reply.id = this.id;
        reply.type = Type.result;
        return reply;
    }
    
    public Iq error() {
        // TODO: implement other types
        Iq error = new Iq();
        error.from = to;
        error.to = from;
        error.id = this.id;
        error.type = Type.error;        
        return error;
    }

    public static Iq parse(XmlPullParser parser) throws XmlPullParserException, java.io.IOException {
        Iq iq = new Iq();
        iq.parseStanza(parser);

        while (parser.next() == XmlPullParser.START_TAG) {
            final String xmlns = parser.getNamespace();
            if (xmlns != null) {
                ChildElement child = Stanza.parseChild(xmlns, parser);
                if (child != null) {
                    iq.addChild(child);
                } else {
                    XmlUtils.skip(parser);
                }
            } else {
                XmlUtils.skip(parser);
            }
        }
        return iq;
    }

    @Override
    public String toString() {
        String str = "<" + TagName + super.toString() + ">";
        for (Enumeration e = childs.elements(); e.hasMoreElements();) {
            str += e.nextElement().toString();
        }
        if (type.equals(Type.error)) {
            str += "<error type='cancel'><service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas' /></error>";
        }
        str += "</" + TagName + ">";
        return str;
    }
}
