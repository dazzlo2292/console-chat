package ru.otus.console.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private final int port;
    private final List<ClientHandler> clients;

    public Server(int port) throws IOException {
        this.port = port;
        this.clients = new ArrayList<>();
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
        boolean isFound = false;
        for (ClientHandler client : clients) {
            if (client.getUserName().equals(dstName)) {
                isFound = true;
                client.send(message);
            }
        }
        if (isFound) {
            srcClient.send(message);
        } else {
            srcClient.send("userName not found!");
        }
    }

    public synchronized void broadcastMessages(String message){
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    private synchronized void subscribe(ClientHandler client) {
        broadcastMessages(client.getUserName() + " joined to the chat");
        clients.add(client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastMessages(client.getUserName() + " left the chat");
    }
}
