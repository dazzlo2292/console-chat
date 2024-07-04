package ru.otus.console.chat.db;

public class Queries {
    public final static String Q_GET_ALL_ACTIVE_USERS = """
            SELECT ut.id, ut.login, ut.password, ut.user_name FROM users_tab ut
            WHERE ut.block_fl = 'N'
            """;

    public final static String Q_GET_ROLES_OF_USER = """
            SELECT rt.name FROM roles_tab rt
            JOIN users_roles_tab urt ON urt.role_id = rt.id
            JOIN users_tab ut ON ut.id = urt.user_id
            WHERE ut.user_name = ?
            """;

    public final static String Q_GET_USER_BY_LOGIN = """
            SELECT 1 FROM users_tab ut
            WHERE ut.login = ? AND ut.block_fl = 'N'
            """;

    public final static String Q_GET_USER_BY_USERNAME = """
            SELECT 1 FROM users_tab ut
            WHERE ut.user_name = ? AND ut.block_fl = 'N'
            """;

    public final static String Q_GET_USERNAME_BY_LOGIN_AND_PASSWORD = """
            SELECT ut.user_name FROM users_tab ut
            WHERE ut.login = ?
            AND ut.password = ?
            AND ut.block_fl = 'N'
            """;

    public final static String Q_ADD_USER = """
            INSERT INTO users_tab (login, password, user_name) VALUES (?, ?, ?);
            INSERT INTO users_roles_tab
            (SELECT ur.id, (SELECT rt.ID FROM roles_tab rt WHERE rt.name = 'USER') FROM users_tab ur where ur.login = ?)
            """;

    public final static String Q_BLOCK_OR_UNBLOCK_USER = """
            UPDATE users_tab SET block_fl = ? WHERE user_name = ?
            """;
}
