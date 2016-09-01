package com.juick.xmpp.extensions;

import com.juick.xmpp.StanzaChild;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by vitalyster on 31.08.2016.
 */
public class ChatState implements StanzaChild {

    public State getValue() {
        return value;
    }

    public void setValue(State value) {
        this.value = value;
    }

    public enum State {
        active,
        composing,
        paused,
        inactive,
        gone
    }

    private State value;

    @Override
    public String getXMLNS() {
        return "http://jabber.org/protocol/chatstates";
    }

    @Override
    public StanzaChild parse(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        String chatstate = parser.getName();
        ChatState state = new ChatState();
        try {
            state.setValue(State.valueOf(chatstate));
        } catch (IllegalArgumentException e) {
            state.setValue(State.active);
        }
        return state;
    }
}
