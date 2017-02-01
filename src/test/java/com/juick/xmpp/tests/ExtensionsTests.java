package com.juick.xmpp.tests;

import com.juick.xmpp.Message;
import com.juick.xmpp.extensions.ChatState;
import com.juick.xmpp.extensions.ReceiptsRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Created by vitalyster on 31.08.2016.
 */
public class ExtensionsTests {
    @Mock
    Message.MessageListener testListener;
    @Captor
    ArgumentCaptor<Message> messageCaptor;
    @Before
    public void parseMessage() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void chatstateTest() {
        TestStream dummyStream = new TestStream(
                "<stream xmlns='jabber:client'><message to='vasya@localhost'>" +
                        "<active xmlns='http://jabber.org/protocol/chatstates'/>" +
                        "<request xmlns='urn:xmpp:receipts'/>" +
                        "</message>" +
                        "<message to='vasya@localhost'>" +
                        "<passive xmlns='http://jabber.org/protocol/chatstates'/>" +
                        "</message>");
        dummyStream.addChildParser(new ChatState());
        dummyStream.addChildParser(new ReceiptsRequest());
        dummyStream.addListener(testListener);
        dummyStream.connect();
        messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(testListener, times(2)).onMessage(messageCaptor.capture());
        List<Message> msgs = messageCaptor.getAllValues();
        ChatState chatState = (ChatState) msgs.get(0).getChild("http://jabber.org/protocol/chatstates");
        assertEquals("active chatstate", ChatState.State.active, chatState.getValue());
        ChatState errorState = (ChatState) msgs.get(1).getChild("http://jabber.org/protocol/chatstates");
        assertEquals("undefined chatstate should be considered active", ChatState.State.active, errorState.getValue());
        ReceiptsRequest receiptsRequest = (ReceiptsRequest) msgs.get(0).getChild(ReceiptsRequest.XMLNS);
        assertEquals("message have receipt request", true, receiptsRequest != null);
        ReceiptsRequest noReceiptsRequest = (ReceiptsRequest) msgs.get(1).getChild(ReceiptsRequest.XMLNS);
        assertEquals("message have no receipt request", true, noReceiptsRequest == null);
    }
}
