package com.juick.xmpp.extensions;

import com.juick.xmpp.utils.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by vitalyster on 03.02.2017.
 */
public class StreamError {

    public static final String XMLNS = "urn:ietf:params:xml:ns:xmpp-streams";

    private String condition;

    public StreamError() {}

    public StreamError(String condition) {
        this.condition = condition;
    }

    public static StreamError parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        StreamError streamError = new StreamError();
        while (parser.next() == XmlPullParser.START_TAG) {
            final String tag = parser.getName();
            final String xmlns = parser.getNamespace();
            if (xmlns.equals(XMLNS)) {
                streamError.setCondition(tag);
            } else {
                XmlUtils.skip(parser);
            }
        }
        return streamError;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return String.format("<stream:error><%s xmlns='%s'/></stream:error>", condition, XMLNS);
    }
}
