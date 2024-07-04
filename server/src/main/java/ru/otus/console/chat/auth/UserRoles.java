package ru.otus.console.chat.auth;


public enum UserRoles {
    USER("USER","Пользователь"),
    ADMIN("ADMIN","Администратор");

    private String name;
    private String description;

    UserRoles(String name, String description) {
        this.name =
        this.description = description;
    }
}
