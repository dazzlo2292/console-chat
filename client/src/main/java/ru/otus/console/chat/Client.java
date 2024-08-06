package ru.otus.console.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final Socket socket;
    private final  DataInputStream in;
    private final  DataOutputStream out;
    private final Scanner scanner;
    private boolean active;

    public Client() throws IOException {
        this.socket = new Socket("localhost", 8089);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.scanner = new Scanner(System.in);
        this.active = true;

        System.out.println("Connected to server");

        new Thread(() -> {
            try {
                while (true) {
                    String input = in.readUTF();
                    if (input.equals("/exit_ok")) {
                        break;
                    }
                    if (input.startsWith("/auth_ok ")) {
                        System.out.println("Authentication completed. UserName: " + input.split(" ")[1]);
                        continue;
                    }
                    if (input.startsWith("/reg_ok ")) {
                        System.out.println("Registration completed. UserName: " + input.split(" ")[1]);
                        continue;
                    }
                    if (input.equals("/disconnect")) {
                        System.out.println("Admin kick you of chat");
                        out.writeUTF("/disconnect");
                        active = false;
                        break;
                    }
                    if (input.equals("/you_afk")) {
                        System.out.println("You are disconnected by inactivity");
                        out.writeUTF("/afk");
                        active = false;
                        break;
                    }
                    if (input.equals("/shutdown_ok")) {
                        break;
                    }
                    System.out.println(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();

        while (active) {
            String message = scanner.nextLine();
            out.writeUTF(message);
            if (message.equals("/exit") || message.equals("/shutdown")) {
                break;
            }
        }
    }

    private void disconnect() {
        scanner.close();

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

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
