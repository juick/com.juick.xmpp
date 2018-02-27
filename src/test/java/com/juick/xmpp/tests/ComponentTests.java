package com.juick.xmpp.tests;

import com.juick.xmpp.StreamComponent;
import com.juick.xmpp.StreamComponentServer;
import com.juick.xmpp.StreamHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xmlpull.v1.XmlPullParserException;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.extensions.receipts.model.MessageDeliveryReceipts;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by vitalyster on 30.01.2017.
 */
public class ComponentTests {
    @Mock
    private
    StreamHandler testListener;

    @Captor
    private
    ArgumentCaptor<String> messageCaptor;
    @Captor
    private
    ArgumentCaptor<Exception> exceptionArgumentCaptor;
    private ExecutorService executorService;
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        executorService = Executors.newFixedThreadPool(2);
    }

    @Test
    public void componentTest() throws IOException, XmlPullParserException, JAXBException {

        ServerSocket serverSocket = mock(ServerSocket.class);
        Socket client = mock(Socket.class);
        Socket server = mock(Socket.class);
        when(serverSocket.accept()).thenReturn(server);

        PipedInputStream serverInputStream = new PipedInputStream();
        PipedOutputStream clientOutputStream = new PipedOutputStream(serverInputStream);
        PipedInputStream clientInputStream = new PipedInputStream();
        PipedOutputStream serverOutputStream = new PipedOutputStream(clientInputStream);

        when(client.getInputStream()).thenReturn(clientInputStream);
        when(client.getOutputStream()).thenReturn(clientOutputStream);
        when(server.getInputStream()).thenReturn(serverInputStream);
        when(server.getOutputStream()).thenReturn(serverOutputStream);

        Jid localhost = Jid.of("localhost");
        StreamHandler serverListener = mock(StreamHandler.class);
        when(serverListener.filter(null, localhost)).thenReturn(false);

        final StreamComponentServer[] componentServer = new StreamComponentServer[1];
        executorService.submit(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                componentServer[0] = new StreamComponentServer(clientSocket.getInputStream(), clientSocket.getOutputStream(), "secret");
                componentServer[0].setHandler(serverListener);
                componentServer[0].connect();
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        });

        StreamComponent component = new StreamComponent(localhost, client.getInputStream(), client.getOutputStream(), "secret");
        component.setHandler(testListener);
        executorService.submit(component::connect);

        verify(testListener, timeout(5000).times(1)).ready();

        componentServer[0].send("<message from='vasya@localhost' to='petya@localhost'>" +
                "<body>Тест</body><request xmlns='urn:xmpp:receipts'/></message>");

        verify(testListener, timeout(5000).times(1)).stanzaReceived(messageCaptor.capture());
        Message message = (Message) parse(messageCaptor.getValue());
        assertEquals("Тест", message.getBody());
        MessageDeliveryReceipts receipts = message.getExtension(MessageDeliveryReceipts.Request.class);
        assertThat(message, is(notNullValue()));
        assertThat(receipts, is(notNullValue()));
        component.send("<yo:people/>");
        verify(testListener, timeout(5000).times(1)).fail(exceptionArgumentCaptor.capture());
        assertEquals("invalid-xml", exceptionArgumentCaptor.getValue().getMessage());
    }
    @Test
    public void serverTest() {

    }

    private Stanza parse(String xml) throws JAXBException {
        Unmarshaller unmarshaller = session().createUnmarshaller();
        return (Stanza) unmarshaller.unmarshal(new StringReader(xml));
    }
    private TestXmppSession session() {
        XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                .build();
        return TestXmppSession.create("localhost", configuration);
    }

}
