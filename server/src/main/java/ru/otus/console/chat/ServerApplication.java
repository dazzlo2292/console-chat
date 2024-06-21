package ru.otus.console.chat;

import java.io.IOException;

public class ServerApplication {
    public static void main(String[] args) throws IOException {
        new Server(8089).start();
    }
}
