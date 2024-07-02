package ru.otus.console.chat.auth;

import ru.otus.console.chat.ClientHandler;
import ru.otus.console.chat.Server;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private final Server server;
    private final Map<String, User> users;

    public InMemoryAuthenticationProvider(Server server) {
        this.server = server;
        this.users = new HashMap<>();
        User admin = new User("admin","admin", "admin");
        admin.addRole(UserRoles.ADMIN);
        users.put(admin.getUserName(), admin);
        users.put("user", new User("user","user", "user"));
    }

    @Override
    public Map<String, User> getUsers() {
        return users;
    }

    @Override
    public void initialize() {
        System.out.println("AuthenticationProvider started. Mode: In-Memory");
    }

    private String getUserNameByLoginAndPassword(String login, String password) {
        for (User user : users.values()) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user.getUserName();
            }
        }
        return null;
    }

    private boolean isLoginExists(String login) {
        for (User user : users.values()) {
            if (user.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserNameExists(String userName) {
        for (User user : users.values()) {
            if (user.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean authentication(ClientHandler clientHandler, String login, String password) {
        String authUserName = getUserNameByLoginAndPassword(login, password);
        if (authUserName == null) {
            clientHandler.send("ERROR — Incorrect login/password");
            return false;
        }
        if (server.isUserNameBusy(authUserName)) {
            clientHandler.send("ERROR — Username is busy");
            return false;
        }
        clientHandler.setUserName(authUserName);
        clientHandler.addUserRole(users.get(authUserName).getRoles());
        server.subscribe(clientHandler);
        clientHandler.send("/auth_ok " + authUserName);
        return true;
    }

    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String userName) {
        if (login.trim().length() < 3 || password.trim().length() < 6 || userName.isEmpty()) {
            clientHandler.send("""
                    ERROR — Incorrect data
                    Requirements:
                    Login — 3+ symbols
                    Password — 6+ symbols
                    UserName — 1+ symbols""");
            return false;
        }
        if (isLoginExists(login)) {
            clientHandler.send("ERROR — Login already exists");
            return false;
        }
        if (isUserNameExists(userName)) {
            clientHandler.send("ERROR — UserName already exists");
            return false;
        }
        users.put(userName,new User(login, password, userName));
        clientHandler.setUserName(userName);
        clientHandler.addUserRole(users.get(userName).getRoles());
        server.subscribe(clientHandler);
        clientHandler.send("/reg_ok " + userName);
        return true;
    }
}
