package ru.otus.console.chat;

public class ServerApplication {
    public static void main(String[] args) throws Exception {
        new Server(8089).start();
    }
}
