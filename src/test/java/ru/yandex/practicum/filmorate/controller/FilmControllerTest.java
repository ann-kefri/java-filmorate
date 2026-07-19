package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private final FilmController controller = new FilmController();

    @Test
    void shouldFailValidationForEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createFilm(film));
        assertEquals("Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldFailValidationForLongDescription() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createFilm(film));
        assertEquals("Описание не может превышать 200 символов", exception.getMessage());
    }

    @Test
    void shouldFailValidationForOldReleaseDate() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createFilm(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldFailValidationForNegativeDuration() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> controller.createFilm(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2020, 5, 15));
        film.setDuration(120);

        Film created = controller.createFilm(film);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
    }
}