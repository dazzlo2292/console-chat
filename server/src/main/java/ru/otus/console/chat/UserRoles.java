package ru.otus.console.chat;

public enum UserRoles {
    USER("Пользователь"),
    ADMIN("Администратор");

    private String description;

    UserRoles(String description) {
        this.description = description;
    }
}
