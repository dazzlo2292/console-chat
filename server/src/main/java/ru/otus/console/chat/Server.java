package ru.otus.console.chat;

import ru.otus.console.chat.auth.AuthenticationProvider;
import ru.otus.console.chat.auth.InMemoryAuthenticationProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final int port;
    private final Map<String, ClientHandler> clients;
    private final AuthenticationProvider authenticationProvider;

    public Server(int port) {
        this.port = port;
        this.clients = new HashMap<>();
        this.authenticationProvider = new InMemoryAuthenticationProvider(this);
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void start() {
        try (ServerSocket socket = new ServerSocket(this.port)) {
            System.out.println("Server started on port " + port);
            authenticationProvider.initialize();
            while (true) {
                Socket connection = socket.accept();
                new ClientHandler(this,connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendWhisperMessage(ClientHandler srcClient, String dstName, String message) {
        if (clients.containsKey(dstName)) {
            clients.get(dstName).send(message);
            srcClient.send(message);
            return;
        }
        srcClient.send("ERROR — userName not found!");
    }

    public synchronized void broadcastMessages(String message){
        for (ClientHandler client : clients.values()) {
            client.send(message);
        }
    }

    public synchronized void subscribe(ClientHandler client) {
        broadcastMessages(client.getUserName() + " joined to the chat");
        clients.put(client.getUserName(), client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client.getUserName());
        broadcastMessages(client.getUserName() + " left the chat");
    }

    public synchronized void disconnectUser(ClientHandler srcClient, String userName) {
        if (clients.containsKey(userName)) {
            clients.get(userName).send("/disconnect");
            return;
        }
        srcClient.send("ERROR — userName not found!");
    }

    public synchronized boolean isUserNameBusy(String userName) {
        return clients.containsKey(userName);
    }
}
