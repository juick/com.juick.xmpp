package com.juick.xmpp;

import rocks.xmpp.addr.Jid;

/**
 * Created by vitalyster on 01.02.2017.
 */
public interface StreamListener {

    void ready();

    void fail(final Exception ex);

    boolean filter(Jid from, Jid to);
}
