package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private final UserController controller = new UserController();

    @Test
    void shouldFailValidationForEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createUser(user));
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
                () -> controller.createUser(user));
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
                () -> controller.createUser(user));
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
                () -> controller.createUser(user));
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
                () -> controller.createUser(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = controller.createUser(user);
        assertEquals("validLogin", created.getName());
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = controller.createUser(user);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
    }
}