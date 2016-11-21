package com.juick.xmpp.extensions;

import com.juick.xmpp.StanzaChild;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by vitalyster on 21.11.2016.
 */
public class ReceiptsRequest implements StanzaChild {
    public final static String XMLNS = "urn:xmpp:receipts";
    public final static String TagName = "request";
    @Override
    public String getXMLNS() {
        return XMLNS;
    }

    @Override
    public StanzaChild parse(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        return new ReceiptsRequest();
    }

    @Override
    public String toString() {
        return String.format("<%s xmlns='%s' />", TagName, XMLNS);
    }
}
