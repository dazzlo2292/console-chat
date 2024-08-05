package ru.otus.console.chat.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.console.chat.ClientHandler;
import ru.otus.console.chat.Server;
import ru.otus.console.chat.db.Queries;

import java.sql.*;
import java.util.*;

public class DatabaseAuthenticationProvider implements AuthenticationProvider{
    private static final Logger logger = LogManager.getLogger(DatabaseAuthenticationProvider.class.getName());

    private final Server server;
    private final Connection connection;
    private final Statement statement;

    public DatabaseAuthenticationProvider(Server server, String url, String login, String password) throws SQLException {
        this.server = server;
        this.connection = DriverManager.getConnection(url, login, password);
        this.statement = connection.createStatement();
    }

    @Override
    public void initialize() {
        logger.info("AuthenticationProvider started. Mode: Database");
    }

    private String getUserNameByLoginAndPassword(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_GET_USERNAME_BY_LOGIN_AND_PASSWORD)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("user_name");
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
        return null;
    }

    private boolean isLoginExists(String login) {
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_GET_USER_BY_LOGIN)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                return true;
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
        return false;
    }

    private boolean isUserNameExists(String userName) {
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_GET_USER_BY_USERNAME)) {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.isBeforeFirst()) {
                return true;
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
        return false;
    }

    @Override
    public boolean authentication(ClientHandler clientHandler, String login, String password) {
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
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_GET_ROLES_OF_USER)) {
            ps.setString(1, authUserName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clientHandler.setUserRole(new Role(rs.getString("name")));
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
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
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_ADD_USER)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ps.setString(3, userName);
            ps.setString(4, login);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
        clientHandler.setUserName(userName);
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_GET_ROLES_OF_USER)) {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                clientHandler.setUserRole(new Role(rs.getString("name")));
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
        server.subscribe(clientHandler);
        clientHandler.send("/reg_ok " + userName);
        return true;
    }

    @Override
    public void blockOrUnblockUser(String blockStatus, String userName) {
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_BLOCK_OR_UNBLOCK_USER)) {
            ps.setString(1, blockStatus);
            ps.setString(2, userName);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
    }

    @Override
    public void setUserName(String currentUserName, String newUserName) {
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_SET_USERNAME)) {
            ps.setString(1, newUserName);
            ps.setString(2, currentUserName);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
    }

    @Override
    public Map<String, User> getUsers() {
        Map<String, User> users = new HashMap<>();
        try (ResultSet rs = statement.executeQuery(Queries.Q_GET_ALL_ACTIVE_USERS)) {
            while (rs.next()) {
                User user = new User(rs.getInt("id"), rs.getString("login"),
                        rs.getString("password"), rs.getString("user_name"));
                users.put(rs.getString("user_name"),user);
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }

        for (User user : users.values()) {
            user.setRole(getRolesOfUser(user.getUserName()));
        }
        return users;
    }

    public Set<Role> getRolesOfUser(String userName) {
        Set<Role> roles = new HashSet<>();
        try (PreparedStatement ps = connection.prepareStatement(Queries.Q_GET_ROLES_OF_USER)) {
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                roles.add(new Role(rs.getString("name")));
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса", e);
        }
        return roles;
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Ошибка при закрытии соединения с базой данных", e);
        }

        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logger.error("Ошибка при закрытии соединения с базой данных", e);
        }
    }
}