package com.juick.xmpp.tests;

import com.juick.xmpp.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rocks.xmpp.addr.Jid;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by vitalyster on 30.01.2017.
 */
public class ComponentTests {
    @Mock
    StreamListener testListener;
    @Mock
    Message.MessageListener messageListener;

    @Captor
    ArgumentCaptor<Message> messageCaptor;
    @Captor
    ArgumentCaptor<Exception> exceptionArgumentCaptor;
    ExecutorService executorService;
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        executorService = Executors.newFixedThreadPool(2);
    }

    @Test
    public void componentTest() throws IOException, InterruptedException {

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
        StreamListener serverListener = mock(StreamListener.class);
        when(serverListener.filter(null, localhost)).thenReturn(false);

        final StreamComponentServer[] componentServer = new StreamComponentServer[1];
        executorService.submit(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                componentServer[0] = new StreamComponentServer(clientSocket.getInputStream(), clientSocket.getOutputStream(), "secret");
                componentServer[0].addListener(serverListener);
                componentServer[0].connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        StreamComponent component = new StreamComponent(localhost, client.getInputStream(), client.getOutputStream(), "secret");
        component.addListener(testListener);
        component.addListener(messageListener);
        executorService.submit(component::connect);

        verify(testListener, timeout(5000).times(1)).ready();

        Message msg = new Message();
        msg.from = Jid.of("vasya@localhost");
        msg.to = Jid.of("masha@localhost");
        msg.body = "test";
        componentServer[0].send(msg);

        verify(messageListener, times(1)).onMessage(messageCaptor.capture());
        Message received = messageCaptor.getValue();
        assertEquals("test", received.body);
        component.send("<yo:people/>");
        verify(testListener, timeout(5000).times(1)).fail(exceptionArgumentCaptor.capture());
        assertEquals("invalid-xml", exceptionArgumentCaptor.getValue().getMessage());
    }

}
