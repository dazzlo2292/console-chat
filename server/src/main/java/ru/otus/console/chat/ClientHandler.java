package ru.otus.console.chat;

import ru.otus.console.chat.auth.UserRoles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String userName;
    private Set<UserRoles> userRoles;

    private final static String SEPARATOR = "--------------------------------------------------------";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void addUserRole(UserRoles userRole) {
        this.userRoles.add(userRole);
    }

    public void addUserRole(Set<UserRoles> userRoles) {
        this.userRoles.addAll(userRoles);
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());
        this.userRoles = new HashSet<>();

        new Thread(() -> {
            try {
                System.out.println("Client connected");
                while (true) {
                    send(SEPARATOR + "\nLog in or registration account\n" +
                            "Auth format         — /auth {login} {password}\n" +
                            "Registration format — /reg {login} {password} {userName}\n" + SEPARATOR);
                    String input = read();
                    if (input.equals("/exit")) {
                        send("/exit_ok");
                        return;
                    }
                    if (input.startsWith("/auth ")) {
                        String[] parts = input.split(" ");
                        if (parts.length != 3) {
                            send("ERROR — Incorrect command format");
                            continue;
                        }
                        if (server.getAuthenticationProvider().authentication(this, parts[1], parts[2])) {
                            break;
                        }
                        continue;
                    }
                    if (input.startsWith("/reg ")) {
                        String[] parts = input.split(" ");
                        if (parts.length != 4) {
                            send("ERROR — Incorrect command format");
                            continue;
                        }
                        if (server.getAuthenticationProvider().registration(this, parts[1], parts[2], parts[3])) {
                            break;
                        }
                    }
                }
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
                            case "/disconnect":
                                breakLoop = true;
                                break;
                            case "/w":
                                String dstName = parts[1];
                                String message = "[" + userName + " -> " + dstName + "]: " + parts[2];
                                System.out.println(message);
                                server.sendWhisperMessage(this, dstName, message);
                                break;
                            case "/kick":
                                if (userRoles.contains(UserRoles.ADMIN)) {
                                    if (parts.length != 2) {
                                        this.send("ERROR — Incorrect command format");
                                        break;
                                    }
                                    String targetUserName = parts[1];
                                    server.disconnectUser(this, targetUserName);
                                } else {
                                    this.send("ERROR — Permission denied");
                                }
                                break;
                            default:
                                this.send("ERROR — Unknown command");
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
