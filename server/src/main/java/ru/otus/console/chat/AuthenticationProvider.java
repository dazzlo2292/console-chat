package ru.otus.console.chat;

public interface AuthenticationProvider {
    void initialize();
    boolean authentication(ClientHandler clientHandler, String login, String password);
    boolean registration(ClientHandler clientHandler, String login, String password, String userName);
}
