package ru.otus.console.chat.auth;

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
