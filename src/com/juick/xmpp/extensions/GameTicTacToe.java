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
 * http://xmpp.org/extensions/inbox/tictactoe.html
 * @author Ugnich Anton
 */
public class GameTicTacToe implements ChildElement {

    public final static String TagName = "move";
    public final static String XMLNS = "http://jabber.org/protocol/games/tictactoe";
    public int id = 0;
    public int row = 0;
    public int col = 0;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    public static GameTicTacToe parse(final XmlPullParser parser) throws XmlPullParserException, IOException {
        GameTicTacToe ttt = new GameTicTacToe();
        String str = parser.getAttributeValue(null, "id");
        if (str != null) {
            ttt.id = Integer.parseInt(str);
        }
        str = parser.getAttributeValue(null, "row");
        if (str != null) {
            ttt.row = Integer.parseInt(str);
        }
        str = parser.getAttributeValue(null, "col");
        if (str != null) {
            ttt.col = Integer.parseInt(str);
        }

        XmlUtils.skip(parser);

        if (ttt.id > 0 && ttt.row > 0 && ttt.col > 0) {
            return ttt;
        } else {
            return null;
        }
    }

    public static String xml(int id, int row, int col) {
        return "<" + TagName + " xmlns='" + XMLNS + "' id='" + id + "' row='" + row + "' col='" + col + "'/>";
    }
}
