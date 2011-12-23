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
 * http://xmpp.org/extensions/inbox/instant-gaming.html
 * @author Ugnich Anton
 */
public class Games implements ChildElement {

    public final static String XMLNS = "http://jabber.org/protocol/games";
    public final static String TagName = "";
    public final static int INVITE = 1;
    public final static int DECLINE = 2;
    public final static int JOIN = 3;
    public final static int TURN = 4;
    public final static int SAVED = 5;
    public final static int SAVE = 6;
    public final static int TERMINATE = 7;
    public int action = 0;
    public String game = null;
    public Object turn = null;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public static Games parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Games games = new Games();

        String tag = parser.getName();
        if (tag.equals("turn")) {
            games.action = TURN;
            while (parser.next() == XmlPullParser.START_TAG) {
                final String xmlns = parser.getAttributeValue(null, "xmlns");
                if (xmlns != null && xmlns.equals(GameTicTacToe.XMLNS)) {
                    games.turn = GameTicTacToe.parse(parser);
                } else {
                    XmlUtils.skip(parser);
                }
            }
        } else if (tag.equals("invite")) {
            games.action = INVITE;
            while (parser.next() == XmlPullParser.START_TAG) {
                tag = parser.getName();
                if (tag.equals("game")) {
                    games.game = parser.getAttributeValue(null, "var");
                }
                XmlUtils.skip(parser);
            }
        } else {
            if (tag.equals("decline")) {
                games.action = DECLINE;
            } else if (tag.equals("join")) {
                games.action = JOIN;
            } else if (tag.equals("saved")) {
                games.action = SAVED;
            } else if (tag.equals("save")) {
                games.action = SAVE;
            } else if (tag.equals("terminate")) {
                games.action = TERMINATE;
            }
            XmlUtils.skip(parser);
        }

        return games.action > 0 ? games : null;
    }

    @Override
    public String toString() {
        return "";
    }
}
