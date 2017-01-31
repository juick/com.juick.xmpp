package com.juick.xmpp.tests;

import com.juick.xmpp.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    Stream.StreamListener testListener;
    @Mock
    Message.MessageListener messageListener;

    @Captor
    ArgumentCaptor<Message> messageCaptor;
    ExecutorService executorService;
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        executorService = Executors.newFixedThreadPool(2);
    }

    @Test
    public void componentTest() throws IOException, InterruptedException {
        ServerSocket serverSocket = mock(ServerSocket.class);
        final StreamComponentServer[] componentServer = new StreamComponentServer[1];
        executorService.submit(() -> {
            try {
                Socket client = serverSocket.accept();
                componentServer[0] = new StreamComponentServer(
                        new JID("localhost"), new JID("localhost"), client.getInputStream(), client.getOutputStream(), "secret");
                componentServer[0].startParsing();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
        StreamComponent component = new StreamComponent(new JID("localhost"), client.getInputStream(), client.getOutputStream(), "secret");
        component.addListener(testListener);
        component.addListener(messageListener);
        executorService.submit(component::startParsing);

        verify(testListener, timeout(10000).times(1)).onStreamReady();

        Message msg = new Message();
        msg.from = new JID("vasya@localhost");
        msg.to = new JID("masha@localhost");
        msg.body = "test";
        componentServer[0].send(msg);

        verify(messageListener, times(1)).onMessage(messageCaptor.capture());
        Message received = messageCaptor.getValue();
        assertEquals("test", received.body);
    }

}
