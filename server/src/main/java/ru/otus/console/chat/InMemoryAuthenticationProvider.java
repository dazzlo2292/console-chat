package ru.otus.console.chat;

import java.util.HashMap;
import java.util.Map;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    public class User {
        private final String login;
        private final String password;
        private final String userName;
        private UserRoles role;

        public User(String login, String password, String userName) {
            this.login = login;
            this.password = password;
            this.userName = userName;
            this.role = UserRoles.USER;
        }

        public UserRoles getRole() {
            return role;
        }

        public void setRole(UserRoles role) {
            this.role = role;
        }
    }

    private final Server server;
    private final Map<String, User> users;

    public InMemoryAuthenticationProvider(Server server) {
        this.server = server;
        this.users = new HashMap<>();
        User admin = new User("admin","admin", "admin");
        admin.setRole(UserRoles.ADMIN);
        users.put(admin.userName, admin);
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
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.userName;
            }
        }
        return null;
    }

    private boolean isLoginExists(String login) {
        for (User user : users.values()) {
            if (user.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserNameExists(String userName) {
        for (User user : users.values()) {
            if (user.userName.equals(userName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean authentication(ClientHandler clientHandler,String login, String password) {
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
        server.subscribe(clientHandler);
        clientHandler.send("/reg_ok " + userName);
        return true;
    }
}
