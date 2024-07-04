package ru.otus.console.chat.auth;

import java.util.HashSet;
import java.util.Set;

public class User {
    private final int id;
    private final String login;
    private final String password;
    private final String userName;
    private Set<Role> roles;

    public User(int id, String login, String password, String userName) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.userName = userName;
        this.roles = new HashSet<>();
        roles.add(new Role(UserRoles.USER.name()));
    }

    public int getId() {
        return id;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRole(Role role) {
        this.roles.add(role);
    }

    public void setRole(Set<Role> roles) {
        this.roles.addAll(roles);
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
