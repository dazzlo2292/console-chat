package ru.otus.console.chat.auth;

import ru.otus.console.chat.ClientHandler;

import java.util.Map;
import java.util.Set;

public interface AuthenticationProvider extends AutoCloseable{
    void initialize();

    boolean authentication(ClientHandler clientHandler, String login, String password);

    boolean registration(ClientHandler clientHandler, String login, String password, String userName);

    Map<String, User> getUsers();

    Set<String> getUsersForUnblock();

    void blockOrUnblockUser(String status, int days, String userName);

    void setUserName(String currentUserName, String newUserName);
}
