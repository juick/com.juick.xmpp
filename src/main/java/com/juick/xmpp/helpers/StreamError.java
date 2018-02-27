package com.juick.xmpp.helpers;

import com.juick.xmpp.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static com.juick.xmpp.StreamNamespaces.NS_XMPP_STREAMS;

/**
 * Created by vitalyster on 03.02.2017.
 */
public class StreamError {

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
            if (xmlns.equals(NS_XMPP_STREAMS)) {
                streamError.condition = tag;
            } else {
                XmlUtils.skip(parser);
            }
        }
        return streamError;
    }

    public String getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return String.format("<stream:error><%s xmlns='%s'/></stream:error>", condition, NS_XMPP_STREAMS);
    }
}
