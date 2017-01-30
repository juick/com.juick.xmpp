package com.juick.xmpp.extensions;

import com.juick.xmpp.utils.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by vitalyster on 30.01.2017.
 */
public class Handshake {
    private String value;

    public static Handshake parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.next();
        Handshake handshake = new Handshake();
        handshake.setValue(XmlUtils.getTagText(parser));
        return handshake;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        String str = "<handshake";
        if (getValue() != null) {
            str += ">" + getValue() + "</handshake>";
        } else {
            str += "/>";
        }
        return str;
    }
}
