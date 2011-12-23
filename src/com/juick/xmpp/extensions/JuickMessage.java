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
 *
 * @author Ugnich Anton
 */
public class JuickMessage extends com.juick.Message implements ChildElement {

    public final static String XMLNS = "http://juick.com/message";
    public final static String TagName = "juick";

    public JuickMessage() {
    }

    public JuickMessage(com.juick.Message msg) {
        super(msg);
    }

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public static JuickMessage parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        JuickMessage jmsg = new JuickMessage();

        final String sMID = parser.getAttributeValue(null, "mid");
        if (sMID != null) {
            jmsg.MID = Integer.parseInt(sMID);
        }
        final String sRID = parser.getAttributeValue(null, "rid");
        if (sRID != null) {
            jmsg.RID = Integer.parseInt(sRID);
        }
        final String sPrivacy = parser.getAttributeValue(null, "privacy");
        if (sPrivacy != null) {
            jmsg.Privacy = Integer.parseInt(sPrivacy);
        }
        jmsg.TimestampString = parser.getAttributeValue(null, "ts");

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            final String xmlns = parser.getNamespace();
            if (tag.equals("body")) {
                jmsg.Text = XmlUtils.getTagText(parser);
            } else if (tag.equals(JuickUser.TagName) && xmlns != null && xmlns.equals(JuickUser.XMLNS)) {
                jmsg.User = JuickUser.parse(parser);
            } else if (tag.equals("tag")) {
                jmsg.tags.addElement(XmlUtils.getTagText(parser));
            } else {
                XmlUtils.skip(parser);
            }
        }
        return jmsg;
    }

    @Override
    public String toString() {
        String ret = new String();

        ret = "<" + TagName + " xmlns=\"" + XMLNS + "\"";
        if (MID > 0) {
            ret += " mid=\"" + MID + "\"";
        }
        if (RID > 0) {
            ret += " rid=\"" + RID + "\"";
        }
        ret += " privacy=\"" + Privacy + "\"";
        if (TimestampString != null) {
            ret += " ts=\"" + TimestampString + "\"";
        }
        ret += ">";
        if (User != null) {
            ret += User.toString();
        }
        if (Text != null) {
            ret += "<body>" + XmlUtils.escape(Text) + "</body>";
        }
        if (!tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                ret += "<tag>" + XmlUtils.escape(tags.elementAt(i).toString()) + "</tag>";
            }
        }
        ret += "</" + TagName + ">";

        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JuickMessage)) {
            return false;
        }
        JuickMessage jmsg = (JuickMessage) obj;
        return (this.MID == jmsg.MID && this.RID == jmsg.RID);
    }

    @Override
    public int compareTo(Object obj) throws ClassCastException {
        if (!(obj instanceof JuickMessage)) {
            throw new ClassCastException();
        }
        JuickMessage jmsg = (JuickMessage) obj;

        if (this.MID != jmsg.MID) {
            if (this.MID > jmsg.MID) {
                return -1;
            } else {
                return 1;
            }
        }

        if (this.RID != jmsg.RID) {
            if (this.RID < jmsg.RID) {
                return -1;
            } else {
                return 1;
            }
        }

        return 0;
    }
}
