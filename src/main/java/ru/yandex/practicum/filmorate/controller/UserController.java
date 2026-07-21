package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public User createUser(@RequestBody User user) {
        validateUser(user);
        user.setId(nextId++);
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя было пустым, установлено значение логина: {}", user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (user.getId() == 0 || !users.containsKey(user.getId())) {
            log.error("Пользователь с id {} не найден", user.getId());
            throw new ValidationException("Пользователь не найден");
        }
        validateUser(user);
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя было пустым, установлено значение логина: {}", user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Обновлен пользователь: {}", user);
        return user;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получение всех пользователей, количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.error("Email пустой");
            throw new ValidationException("Электронная почта не может быть пустой");
        }

        if (!user.getEmail().contains("@")) {
            log.error("Email не содержит символ @");
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            log.error("Логин пустой");
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            log.error("Логин содержит пробелы");
            throw new ValidationException("Логин не может содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}