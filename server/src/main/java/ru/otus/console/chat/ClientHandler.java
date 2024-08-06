package ru.otus.console.chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.console.chat.jobs.CheckAfkJob;
import ru.otus.console.chat.auth.Role;
import ru.otus.console.chat.auth.UserRoles;
import ru.otus.console.chat.auth.info.Commands;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class.getName());
    private final CheckAfkJob checkAfkJob;

    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String userName;
    private final Set<Role> userRoles;

    private final static String SEPARATOR = "--------------------------------------------------------";

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserRole(Role userRole) {
        this.userRoles.add(userRole);
    }

    public void setUserRole(Set<Role> userRoles) {
        this.userRoles.addAll(userRoles);
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());
        this.userRoles = new HashSet<>();

        checkAfkJob = new CheckAfkJob(this, 1200);

        server.getConnectionsPool().execute(checkAfkJob::run);

        server.getConnectionsPool().execute(()-> {
            try {
                logger.info("Client connected");
                while (true) {
                    send(SEPARATOR + "\nLog in or registration account\n" + "Info: /help\n" + SEPARATOR);
                    String input = read();
                    if (input.equals("/help")) {
                        send(Commands.COMMANDS_BEFORE_AUTH);
                        continue;
                    }
                    if (input.equals("/exit")) {
                        send("/exit_ok");
                        return;
                    }
                    if (input.equals("/afk")) {
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
                            case "/help":
                                this.send(Commands.COMMANDS_AFTER_AUTH);
                            case "/exit":
                                send("/exit_ok");
                                breakLoop = true;
                                break;
                            case "/disconnect", "/afk":
                                breakLoop = true;
                                break;
                            case "/w":
                                String dstName = parts[1];
                                String message = "[" + userName + " -> " + dstName + "]: " + parts[2];
                                System.out.println(message);
                                server.sendWhisperMessage(this, dstName, message);
                                break;
                            case "/activelist":
                                if (parts.length > 1) {
                                    this.send("ERROR — Incorrect command format");
                                    break;
                                }
                                server.sendActiveClientsList(this);
                                break;
                            case "/changenick":
                                if (parts.length != 2) {
                                    this.send("ERROR — Incorrect command format");
                                    break;
                                }
                                String oldUserName = this.getUserName();
                                String newUserName = parts[1];
                                server.getAuthenticationProvider().setUserName(oldUserName, newUserName);
                                this.setUserName(newUserName);
                                server.sendInfoAfterChangeUserName(oldUserName, newUserName);
                                this.send("Your userName is changed to \"" + newUserName + "\"");
                                break;
                            case "/ban":
                                if (userRoles.contains(new Role(UserRoles.ADMIN.name()))) {
                                    if (parts.length == 2) {
                                        String targetUserName = parts[1];
                                        server.disconnectUser(this, targetUserName);
                                        server.getAuthenticationProvider().blockOrUnblockUser("Y", -1, targetUserName);
                                        this.send("Username \"" + targetUserName + "\" blocked");
                                        break;
                                    }
                                    if (parts.length == 3) {
                                        String targetUserName = parts[1];
                                        try {
                                            int daysCount = Integer.parseInt(parts[2]);
                                            server.disconnectUser(this, targetUserName);
                                            server.getAuthenticationProvider().blockOrUnblockUser("Y", daysCount, targetUserName);
                                            this.send("Username \"" + targetUserName + "\" blocked");
                                            break;
                                        } catch (NumberFormatException e) {
                                            this.send("ERROR — Incorrect [days] parameter type");
                                            break;
                                        }
                                    }
                                    this.send("ERROR — Incorrect command format");
                                } else {
                                    this.send("ERROR — Permission denied");
                                }
                                break;
                            case "/unblock":
                                if (userRoles.contains(new Role(UserRoles.ADMIN.name()))) {
                                    if (parts.length != 2) {
                                        this.send("ERROR — Incorrect command format");
                                        break;
                                    }
                                    String targetUserName = parts[1];
                                    server.getAuthenticationProvider().blockOrUnblockUser("N", 0, targetUserName);
                                    this.send("Username \"" + targetUserName + "\" unblocked");
                                } else {
                                    this.send("ERROR — Permission denied");
                                }
                                break;
                            case "/shutdown":
                                if (userRoles.contains(new Role(UserRoles.ADMIN.name()))) {
                                    if (parts.length != 1) {
                                        this.send("ERROR — Incorrect command format");
                                        break;
                                    }
                                    send("/shutdown_ok");
                                    server.shutdown();
                                    breakLoop = true;
                                    break;
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
                logger.error("Непредвиденная ошибка ввода/вывода", e);
            } finally {
                disconnect();
            }
        });
    }

    public String read() throws IOException {
        String input = in.readUTF();
        checkAfkJob.resetLastActivity();
        return input;
    }

    public void send(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            logger.error("Не удалось отправить сообщение", e);
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("Ошибка при закрытии входного потока", e);
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                logger.error("Ошибка при закрытии исходящего потока", e);
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Ошибка при закрытии сокета", e);
        }
    }
}
