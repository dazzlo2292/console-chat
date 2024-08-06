package ru.otus.console.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.console.chat.auth.AuthenticationProvider;
import ru.otus.console.chat.auth.DatabaseAuthenticationProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int port;
    private final Map<String, ClientHandler> clients;
    private final AuthenticationProvider authenticationProvider;
    private Properties properties;
    private final ExecutorService connectionsPool = Executors.newCachedThreadPool();

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

        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            logger.error("Admin shutdown server");
            broadcastMessages("Admin shutdown server");
        }));
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

    public ExecutorService getConnectionsPool() { return connectionsPool; }

    public void start() throws Exception {
        try (ServerSocket socket = new ServerSocket(port)) {
            logger.info("Server started on port {}", port);
            authenticationProvider.initialize();
            while (true) {
                Socket connection = socket.accept();
                new ClientHandler(this,connection);
            }
        } catch (IOException e) {
            logger.error("Не удалось запустить сервер", e);
        } finally {
            if (authenticationProvider != null) {
                authenticationProvider.close();
            }
            connectionsPool.shutdown();
        }
    }

    public void shutdown() {
        System.exit(0);
    }

    public synchronized void sendWhisperMessage(ClientHandler srcClient, String dstName, String message) {
        if (clients.containsKey(dstName)) {
            String messageWithDateTime = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
            clients.get(dstName).send(messageWithDateTime);
            srcClient.send(messageWithDateTime);
            return;
        }
        srcClient.send("ERROR — userName not found!");
    }

    public synchronized void broadcastMessages(String message){
        String messageWithDateTime = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        for (ClientHandler client : clients.values()) {
            client.send(messageWithDateTime);
        }
    }

    public synchronized void sendActiveClientsList(ClientHandler srcClient) {
        StringBuilder clientList = new StringBuilder();
        for (ClientHandler client : clients.values()) {
            clientList.append(client.getUserName()).append("\r\n");
        }
        srcClient.send(clientList.toString());
    }

    public synchronized void sendInfoAfterChangeUserName(String oldUserName, String newUserName) {
        String message = String.format("[%s] UserName \"%s\" is changed to \"%s\".",
                LocalDateTime.now().format(formatter), oldUserName, newUserName);
        for (ClientHandler client : clients.values()) {
            if (!client.getUserName().equals(newUserName)) {
                client.send(message);
            }
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
