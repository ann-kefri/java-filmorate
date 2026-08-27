package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    List<User> getAll();

    Optional<User> getById(int id);

    void delete(int id);

    List<User> getFriends(int userId);

    void addFriend(int userId, int friendId);

    void confirmFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    List<User> getCommonFriends(int userId, int otherId);
}