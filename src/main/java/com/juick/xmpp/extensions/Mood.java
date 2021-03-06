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

/**
 * http://xmpp.org/extensions/xep-0107.html
 * @author Ugnich Anton
 */
public class Mood implements StanzaChild {

    public final static String XMLNS = "http://jabber.org/protocol/mood";
    public final static String TagName = "mood";
    public static final String[] moodValues = {"afraid", "amazed", "amorous", "angry", "annoyed", "anxious", "aroused", "ashamed", "bored", "brave", "calm", "cautious", "cold", "confident", "confused", "contemplative", "contented", "cranky", "crazy", "creative", "curious", "dejected", "depressed", "disappointed", "disgusted", "dismayed", "distracted", "embarrassed", "envious", "excited", "flirtatious", "frustrated", "grateful", "grieving", "grumpy", "guilty", "happy", "hopeful", "hot", "humbled", "humiliated", "hungry", "hurt", "impressed", "in_awe", "in_love", "indignant", "interested", "intoxicated", "invincible", "jealous", "lonely", "lost", "lucky", "mean", "moody", "nervous", "neutral", "offended", "outraged", "playful", "proud", "relaxed", "relieved", "remorseful", "restless", "sad", "sarcastic", "satisfied", "serious", "shocked", "shy", "sick", "sleepy", "spontaneous", "stressed", "strong", "surprised", "thankful", "thirsty", "tired", "undefined", "weak", "worried"};
    public int moodId = -1;
    public String moodTxt = null;

    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public Mood parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        Mood mood = new Mood();

        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            for (int i = 0; i < moodValues.length; i++) {
                if (tag.equals(moodValues[i])) {
                    mood.moodId = i;
                }
            }
            if (tag.equals("text")) {
                mood.moodTxt = XmlUtils.getTagText(parser);
            } else {
                XmlUtils.skip(parser);
            }
        }
        return mood.moodId > -1 ? mood : null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("<").append(TagName).append(" xmlns='").append(XMLNS).append("'>");
        if (moodId > -1) {
            str.append("<").append(moodValues[moodId]).append("/>");
        }
        if (moodTxt != null && moodTxt.length() > 0) {
            str.append("<text>").append(StringEscapeUtils.escapeXml10(moodTxt)).append("</text>");
        }
        str.append("</").append(TagName).append(">");
        return str.toString();
    }
}
