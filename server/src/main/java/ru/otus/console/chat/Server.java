package ru.otus.console.chat;

import ru.otus.console.chat.auth.AuthenticationProvider;
import ru.otus.console.chat.auth.DatabaseAuthenticationProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Server {
    private final int port;
    private final Map<String, ClientHandler> clients;
    private final AuthenticationProvider authenticationProvider;
    private Properties properties;

    private static final String CONFIG_PATH = "config.properties";

    public Server(int port) throws SQLException, IOException {
        this.port = port;
        this.clients = new HashMap<>();
        getProperties();
        this.authenticationProvider = new DatabaseAuthenticationProvider(
                this,
                properties.getProperty("database_url"),
                properties.getProperty("database_login"),
                properties.getProperty("database_password"));
    }

    private void getProperties() throws IOException {
        properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(CONFIG_PATH);
        properties.load(stream);
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public void start() throws Exception {
        try (ServerSocket socket = new ServerSocket(this.port)) {
            System.out.println("Server started on port " + port);
            authenticationProvider.initialize();
            while (true) {
                Socket connection = socket.accept();
                new ClientHandler(this,connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (authenticationProvider != null) {
                authenticationProvider.close();
            }
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
