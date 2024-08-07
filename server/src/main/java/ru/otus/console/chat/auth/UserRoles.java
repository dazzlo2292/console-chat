package ru.otus.console.chat.auth;


public enum UserRoles {
    USER("USER","Пользователь"),
    ADMIN("ADMIN","Администратор");

    private final String name;
    private final String description;

    UserRoles(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
