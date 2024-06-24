package ru.otus.console.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final String userName;

    private static int userNumber = 1;

    public String getUserName() {
        return userName;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());
        this.userName = "user-" + userNumber;
        userNumber++;

        new Thread(() -> {
            try {
                System.out.println("Client connected");
                while (true) {
                    String input = read();
                    boolean breakLoop = false;
                    if (input.startsWith("/")) {
                        String[] parts = input.split(" ", 3);
                        switch (parts[0]) {
                            case "/exit":
                                send("/exit_ok");
                                breakLoop = true;
                                break;
                            case "/w":
                                String dstName = parts[1];
                                String message = "[" + userName + " -> " + dstName + "]: " + parts[2];
                                System.out.println(message);
                                server.sendWhisperMessage(this, dstName, message);
                                break;
                            default:
                                this.send("Command not found");
                        }
                        if (breakLoop) {
                            break;
                        }
                    } else {
                        String message = userName + ": " + input;
                        System.out.println(message);
                        server.broadcastMessages(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public String read() throws IOException {
        return in.readUTF();
    }

    public void send(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
