package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private UserService userService;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        userController = new UserController(userService);
    }

    @Test
    void shouldFailValidationForEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Электронная почта не может быть пустой", exception.getMessage());
    }

    @Test
    void shouldFailValidationForEmailWithoutAtSymbol() {
        User user = new User();
        user.setEmail("invalidemail.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Электронная почта должна содержать символ @", exception.getMessage());
    }

    @Test
    void shouldFailValidationForEmptyLogin() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldFailValidationForLoginWithSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("invalid login");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldFailValidationForBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void shouldPassValidationForBirthdayToday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.now());

        User created = userController.createUser(user);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(LocalDate.now(), created.getBirthday());
    }

    @Test
    void shouldPassValidationForBirthdayInPast() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userController.createUser(user);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(LocalDate.of(2000, 1, 1), created.getBirthday());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);
        assertEquals("validLogin", created.getName());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);
        assertEquals("validLogin", created.getName());
    }

    @Test
    void shouldPassValidationForLoginWithoutSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("validLogin", created.getLogin());
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("test@example.com", created.getEmail());
        assertEquals("validLogin", created.getLogin());
        assertEquals("Valid Name", created.getName());
        assertEquals(LocalDate.of(1990, 1, 1), created.getBirthday());
    }
}