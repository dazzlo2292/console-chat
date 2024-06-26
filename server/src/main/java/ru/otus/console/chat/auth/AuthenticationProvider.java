package ru.otus.console.chat.auth;

import ru.otus.console.chat.ClientHandler;

import java.util.Map;

public interface AuthenticationProvider {
    void initialize();
    boolean authentication(ClientHandler clientHandler, String login, String password);
    boolean registration(ClientHandler clientHandler, String login, String password, String userName);
    Map<String, User> getUsers();
}
