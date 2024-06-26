package ru.otus.console.chat.auth;

public enum UserRoles {
    USER("Пользователь"),
    ADMIN("Администратор");

    private String description;

    UserRoles(String description) {
        this.description = description;
    }
}
