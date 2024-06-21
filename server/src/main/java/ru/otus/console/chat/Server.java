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

    public synchronized void broadcast(String message){
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    private synchronized void subscribe(ClientHandler client) {
        broadcast(client.getUserName() + " joined to the chat");
        clients.add(client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcast(client.getUserName() + " left the chat");
    }
}
