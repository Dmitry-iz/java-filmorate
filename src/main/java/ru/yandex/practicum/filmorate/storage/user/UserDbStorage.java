package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return getById(user.getId());
    }

    @Override
    public User getById(int id) {
        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("User with id=" + id + " not found");
        }
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        // Проверяем существование пользователей
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        // Проверяем существующие связи
        String checkSql = "SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?";
        String reverseCheckSql = "SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?";

        // Проверяем прямую связь
        List<String> statuses = jdbcTemplate.queryForList(checkSql, String.class, userId, friendId);
        // Проверяем обратную связь
        List<String> reverseStatuses = jdbcTemplate.queryForList(reverseCheckSql, String.class, friendId, userId);

        if (!statuses.isEmpty() && statuses.get(0).equals("confirmed")) {
            return; // Уже друзья
        }

        if (!reverseStatuses.isEmpty()) {
            if (reverseStatuses.get(0).equals("unconfirmed")) {
                // Подтверждаем дружбу с обеих сторон
                jdbcTemplate.update("UPDATE friendship SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?",
                        friendId, userId);
                jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, 'confirmed')",
                        userId, friendId);
            }
        } else {
            // Создаем новую неподтвержденную заявку
            jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, 'unconfirmed')",
                    userId, friendId);
        }
    }

    public boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId) > 0;
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        // Проверяем существование пользователей
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        // Удаляем только связь от userId к friendId (одностороннее удаление)
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        // Запрос возвращает как подтвержденных, так и неподтвержденных друзей
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f ON u.id = f.friend_id " +
                "WHERE f.user_id = ?"; // Убираем условие по статусу
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM friendship f1 " +
                "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                "JOIN users u ON f1.friend_id = u.id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = User.builder()
                .id(rs.getInt("id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();

        user.getFriends().addAll(getUserFriends(user.getId()));
        return user;
    }

    private Set<Integer> getUserFriends(int userId) {
        String sql = "SELECT friend_id FROM friendship WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, userId));
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void confirmFriend(int userId, int friendId) {
        // Проверяем существование пользователей
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        // Проверяем есть ли заявка от друга
        String checkSql = "SELECT status FROM friendship WHERE user_id = ? AND friend_id = ?";
        List<String> statuses = jdbcTemplate.queryForList(checkSql, String.class, friendId, userId);

        if (statuses.isEmpty() || !statuses.get(0).equals("unconfirmed")) {
            throw new ValidationException("Нет заявки в друзья от этого пользователя");
        }

        // Подтверждаем дружбу
        jdbcTemplate.update("UPDATE friendship SET status = 'confirmed' WHERE user_id = ? AND friend_id = ?",
                friendId, userId);
        jdbcTemplate.update("INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, 'confirmed')",
                userId, friendId);
    }

    @Override
    public List<User> getFriendRequests(int userId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f ON u.id = f.user_id " +
                "WHERE f.friend_id = ? AND f.status = 'unconfirmed'";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }
}
