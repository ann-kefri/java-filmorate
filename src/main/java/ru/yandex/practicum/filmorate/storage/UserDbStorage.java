package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        };
    }

    private void loadFriends(User user) {
        if (user == null) return;

        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        List<Integer> friendIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getInt("friend_id"),
                user.getId());

        String sql2 = "SELECT user_id FROM friendships WHERE friend_id = ?";
        List<Integer> friendIds2 = jdbcTemplate.query(sql2,
                (rs, rowNum) -> rs.getInt("user_id"),
                user.getId());

        Set<Integer> allFriends = new HashSet<>();
        allFriends.addAll(friendIds);
        allFriends.addAll(friendIds2);

        user.setFriends(allFriends);
        log.debug("Загружено {} друзей для пользователя {}", allFriends.size(), user.getId());
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        loadFriends(user);
        log.debug("Создан пользователь в БД: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId());

        if (rowsAffected == 0) {
            log.error("Пользователь с id {} не найден", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        loadFriends(user);
        log.debug("Обновлен пользователь в БД: {}", user);
        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper());

        for (User user : users) {
            loadFriends(user);
        }
        return users;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper(), id);
            if (user != null) {
                loadFriends(user);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.debug("Пользователь с id {} удален из БД", id);
    }

    @Override
    public List<User> getFriends(int userId) {
        if (getById(userId).isEmpty()) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        String sql = "SELECT u.* FROM users u " +
                "WHERE u.id IN (" +
                "    SELECT friend_id FROM friendships WHERE user_id = ?" +
                ") OR u.id IN (" +
                "    SELECT user_id FROM friendships WHERE friend_id = ?" +
                ")";
        List<User> friends = jdbcTemplate.query(sql, userRowMapper(), userId, userId);

        // Загружаем друзей для каждого найденного друга
        for (User friend : friends) {
            loadFriends(friend);
        }

        log.info("Найдено {} друзей для пользователя {}", friends.size(), userId);
        return friends;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (getById(userId).isEmpty() || getById(friendId).isEmpty()) {
            log.error("Один из пользователей не найден");
            throw new NotFoundException("Пользователь не найден");
        }

        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count == 0) {
            String sql1 = "INSERT INTO friendships (user_id, friend_id, status_id) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql1, userId, friendId, 1);

            String sql2 = "INSERT INTO friendships (user_id, friend_id, status_id) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql2, friendId, userId, 1);

            log.info("Пользователи {} и {} стали друзьями (взаимно)", userId, friendId);
        }
    }

    @Override
    public void confirmFriend(int userId, int friendId) {
        String sql = "UPDATE friendships SET status_id = 1 WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        jdbcTemplate.update(sql, friendId, userId);
        log.info("Пользователи {} и {} подтвердили дружбу", userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
        log.info("Пользователи {} и {} перестали быть друзьями", userId, friendId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "WHERE u.id IN (" +
                "    SELECT friend_id FROM friendships WHERE user_id = ?" +
                ") AND u.id IN (" +
                "    SELECT friend_id FROM friendships WHERE user_id = ?" +
                ")";
        List<User> commonFriends = jdbcTemplate.query(sql, userRowMapper(), userId, otherId);

        for (User friend : commonFriends) {
            loadFriends(friend);
        }

        return commonFriends;
    }
}