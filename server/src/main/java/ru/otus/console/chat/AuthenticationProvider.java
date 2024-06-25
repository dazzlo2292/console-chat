package ru.otus.console.chat;

import java.util.List;
import java.util.Map;

public interface AuthenticationProvider {
    void initialize();
    boolean authentication(ClientHandler clientHandler, String login, String password);
    boolean registration(ClientHandler clientHandler, String login, String password, String userName);
    Map<String, InMemoryAuthenticationProvider.User> getUsers();
}
