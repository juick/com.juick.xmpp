/*
 * Juick
 * Copyright (C) 2008-2013, Ugnich Anton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.juick.xmpp.extensions;

import com.juick.xmpp.utils.XmlUtils;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Ugnich Anton
 */
public class StreamFeatures {

    public static final int NOTAVAILABLE = -1;
    public static final int AVAILABLE = 0;
    public static final int REQUIRED = 1;
    public int STARTTLS = NOTAVAILABLE;
    public int ZLIB = NOTAVAILABLE;
    public int PLAIN = NOTAVAILABLE;
    public int DIGEST_MD5 = NOTAVAILABLE;
    public int X_GOOGLE_TOKEN = NOTAVAILABLE;
    public int REGISTER = NOTAVAILABLE;

    public static StreamFeatures parse(final XmlPullParser parser) throws XmlPullParserException, IOException {
        StreamFeatures features = new StreamFeatures();
        final int initial = parser.getDepth();
        while (true) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG && parser.getDepth() == initial + 1) {
                final String tag = parser.getName();
                final String xmlns = parser.getNamespace();
                if (tag.equals("starttls") && xmlns != null && xmlns.equals("urn:ietf:params:xml:ns:xmpp-tls")) {
                    features.STARTTLS = AVAILABLE;
                    while (parser.next() == XmlPullParser.START_TAG) {
                        if (parser.getName().equals("required")) {
                            features.STARTTLS = REQUIRED;
                        } else {
                            XmlUtils.skip(parser);
                        }
                    }
                } else if (tag.equals("compression") && xmlns != null && xmlns.equals("http://jabber.org/features/compress")) {
                    while (parser.next() == XmlPullParser.START_TAG) {
                        if (parser.getName().equals("method")) {
                            final String method = XmlUtils.getTagText(parser).toUpperCase();
                            if (method.equals("ZLIB")) {
                                features.ZLIB = AVAILABLE;
                            }
                        } else {
                            XmlUtils.skip(parser);
                        }
                    }
                } else if (tag.equals("mechanisms") && xmlns != null && xmlns.equals("urn:ietf:params:xml:ns:xmpp-sasl")) {
                    while (parser.next() == XmlPullParser.START_TAG) {
                        if (parser.getName().equals("mechanism")) {
                            final String mechanism = XmlUtils.getTagText(parser).toUpperCase();
                            if (mechanism.equals("PLAIN")) {
                                features.PLAIN = AVAILABLE;
                            } else if (mechanism.equals("DIGEST-MD5")) {
                                features.DIGEST_MD5 = AVAILABLE;
                            } else if (mechanism.equals("X-GOOGLE-TOKEN")) {
                                features.X_GOOGLE_TOKEN = AVAILABLE;
                            }
                        } else {
                            XmlUtils.skip(parser);
                        }
                    }
                } else if (tag.equals("register") && xmlns != null && xmlns.equals("http://jabber.org/features/iq-register")) {
                    features.REGISTER = AVAILABLE;
                    XmlUtils.skip(parser);
                } else {
                    XmlUtils.skip(parser);
                }
            } else if (eventType == XmlPullParser.END_TAG && parser.getDepth() == initial) {
                break;
            }
        }
        return features;
    }
}
