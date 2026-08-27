package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.debug("Пользователь сохранен в хранилище: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с id {} не найден в хранилище", user.getId());
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        log.debug("Пользователь обновлен в хранилище: {}", user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void delete(int id) {
        users.remove(id);
        log.debug("Пользователь с id {} удален из хранилища", id);
    }

    @Override
    public List<User> getFriends(int userId) {
        User user = users.get(userId);
        if (user == null) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<User> friends = new ArrayList<>();
        for (Integer friendId : user.getFriends()) {
            User friend = users.get(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return friends;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (friend == null) {
            log.error("Пользователь с id {} не найден", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        user.getFriends().add(friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    @Override
    public void confirmFriend(int userId, int friendId) {
        // В in-memory версии просто добавляем в друзья (взаимно)
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (friend == null) {
            log.error("Пользователь с id {} не найден", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (friend == null) {
            log.error("Пользователь с id {} не найден", friendId);
            throw new NotFoundException("Пользователь с id " + friendId + " не найден");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи {} и {} перестали быть друзьями", userId, friendId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        User user = users.get(userId);
        User other = users.get(otherId);

        if (user == null) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (other == null) {
            log.error("Пользователь с id {} не найден", otherId);
            throw new NotFoundException("Пользователь с id " + otherId + " не найден");
        }

        List<User> commonFriends = new ArrayList<>();
        Set<Integer> userFriends = user.getFriends();
        Set<Integer> otherFriends = other.getFriends();

        for (Integer friendId : userFriends) {
            if (otherFriends.contains(friendId)) {
                User friend = users.get(friendId);
                if (friend != null) {
                    commonFriends.add(friend);
                }
            }
        }
        return commonFriends;
    }
}