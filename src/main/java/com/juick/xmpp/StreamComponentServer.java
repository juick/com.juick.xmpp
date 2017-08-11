package com.juick.xmpp;

import com.juick.xmpp.extensions.Handshake;
import com.juick.xmpp.extensions.XMPPError;
import org.apache.commons.codec.digest.DigestUtils;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by vitalyster on 30.01.2017.
 */
public class StreamComponentServer extends Stream {

    public static final String NS_COMPONENT_ACCEPT = "jabber:component:accept";

    private String streamId, secret;

    public String getStreamId() {
        return streamId;
    }


    public StreamComponentServer(InputStream is, OutputStream os, String password) {
        super(null, null, is, os);
        secret = password;
        streamId = UUID.randomUUID().toString();
    }
    @Override
    public void handshake() throws XmlPullParserException, IOException {
        parser.next();
        if (!parser.getName().equals("stream")
                || !parser.getNamespace(null).equals(NS_COMPONENT_ACCEPT)
                || !parser.getNamespace("stream").equals(NS_STREAM)) {
            throw new IOException("invalid stream");
        }
        Jid domain = Jid.of(parser.getAttributeValue(null, "to"));
        if (listenersStream.stream().anyMatch(l ->
                l.filter(null, domain))) {
            send(new XMPPError(XMPPError.Type.cancel, "forbidden").toString());
            throw new IOException("invalid domain");
        }
        from = domain;
        to = domain;
        send(String.format("<stream:stream xmlns:stream='%s' " +
                "xmlns='%s' from='%s' id='%s'>", NS_STREAM, NS_COMPONENT_ACCEPT, from.asBareJid().toEscapedString(), streamId));
        Handshake handshake = Handshake.parse(parser);
        boolean authenticated = handshake.getValue().equals(DigestUtils.sha1Hex(streamId + secret));
        setLoggedIn(authenticated);
        if (!authenticated) {
            send(new XMPPError(XMPPError.Type.cancel, "not-authorized").toString());
            for (StreamListener streamListener : listenersStream) {
                streamListener.fail(new IOException("stream:stream, failed authentication"));
            }
            return;
        }
        send(new Handshake().toString());
        listenersStream.forEach(StreamListener::ready);
    }
}
