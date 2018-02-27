package com.juick.xmpp;

import com.juick.xmpp.helpers.StreamError;
import rocks.xmpp.addr.Jid;

/**
 * Created by vitalyster on 01.02.2017.
 */
public interface StreamHandler {
    void ready();
    void fail(final Exception ex);
    boolean filter(Jid from, Jid to);
    void stanzaReceived(String stanza);
    boolean accept(StreamServerIn connection, String acceptFrom);
    boolean allowTls(StreamServerIn connection, String acceptFrom);
    void startDialback(StreamServerIn connection, Jid from, String dbKey) throws Exception;
    void starttls(StreamServerIn connection);
    void proceed(StreamServerOut connection);
    void verify(String from, String type, String sid);
    void verifyDialbackKey(String streamId, String from, String dbKey);
    void dialbackError(StreamServerOut connection, StreamError error);
    void finished(Stream connection, boolean dirty);
    boolean securing(StreamServerOut connection);
}
