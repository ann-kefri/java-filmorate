package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User user) {
        userStorage.getById(user.getId())
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", user.getId());
                    return new NotFoundException("Пользователь с id " + user.getId() + " не найден");
                });
        return userStorage.update(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User getUserById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
    }

    public User addFriend(int userId, int friendId) {
        if (userId == friendId) {
            log.error("Нельзя добавить самого себя в друзья");
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", friendId);
                    return new NotFoundException("Пользователь с id " + friendId + " не найден");
                });

        userStorage.addFriend(userId, friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
        return user;
    }

    public User removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        User friend = userStorage.getById(friendId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", friendId);
                    return new NotFoundException("Пользователь с id " + friendId + " не найден");
                });

        userStorage.removeFriend(userId, friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        userStorage.update(user);
        userStorage.update(friend);

        log.info("Пользователи {} и {} перестали быть друзьями", userId, friendId);
        return user;
    }

    public List<User> getFriends(int userId) {
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        userStorage.getById(otherId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", otherId);
                    return new NotFoundException("Пользователь с id " + otherId + " не найден");
                });

        return userStorage.getCommonFriends(userId, otherId);
    }
}