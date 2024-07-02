package ru.otus.console.chat.auth;

import java.util.HashSet;
import java.util.Set;

public class User {
    private final String login;
    private final String password;
    private final String userName;
    private Set<UserRoles> roles;

    public User(String login, String password, String userName) {
        this.login = login;
        this.password = password;
        this.userName = userName;
        this.roles = new HashSet<>();
        roles.add(UserRoles.USER);
    }

    public Set<UserRoles> getRoles() {
        return roles;
    }

    public void addRole(UserRoles role) {
        this.roles.add(role);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }
}
