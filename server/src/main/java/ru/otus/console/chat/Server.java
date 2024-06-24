package ru.otus.console.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private final int port;
    private final Map<String, ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new HashMap<>();
    }

    public void start() {
        try (ServerSocket socket = new ServerSocket(this.port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket connection = socket.accept();
                subscribe(new ClientHandler(this,connection));
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
        srcClient.send("userName not found!");
    }

    public synchronized void broadcastMessages(String message){
        for (Map.Entry<String, ClientHandler> client : clients.entrySet()) {
            ClientHandler currentClient = client.getValue();
            currentClient.send(message);
        }
    }

    private synchronized void subscribe(ClientHandler client) {
        broadcastMessages(client.getUserName() + " joined to the chat");
        clients.put(client.getUserName(), client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastMessages(client.getUserName() + " left the chat");
    }
}
