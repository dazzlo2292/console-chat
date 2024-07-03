package ru.otus.console.chat.auth;

import ru.otus.console.chat.ClientHandler;

import java.util.Map;

public interface AuthenticationProvider extends AutoCloseable{
    void initialize();

    boolean authentication(ClientHandler clientHandler, String login, String password);

    boolean registration(ClientHandler clientHandler, String login, String password, String userName);

    Map<String, User> getUsers();

    void blockOrUnblockUser(String status, String userName);
}
