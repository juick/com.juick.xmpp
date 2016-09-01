package com.juick.xmpp.tests;

import com.juick.xmpp.Stream;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by vitalyster on 31.08.2016.
 */
public class TestStream extends Stream {
    public TestStream(String testOutput) {
        super(null, null, new ByteArrayInputStream(testOutput.getBytes(StandardCharsets.UTF_8)),
                new OutputStream() {
                    private StringBuilder string = new StringBuilder();

                    @Override
                    public void write(int b) throws IOException {
                        this.string.append((char) b);
                    }

                    public String toString() {
                        return this.string.toString();
                    }
                });
    }

    @Override
    public void openStream() throws XmlPullParserException, IOException {
        restartParser();
        parser.next();
    }
}
