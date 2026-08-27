package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private FilmService filmService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private GenreDbStorage genreStorage;
    private MpaRatingDbStorage mpaRatingStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService, genreStorage, mpaRatingStorage);
        genreStorage = new GenreDbStorage(null);
        mpaRatingStorage = new MpaRatingDbStorage(null);
    }

    @Test
    void shouldFailValidationForEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
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
                () -> filmController.createFilm(film));
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
                () -> filmController.createFilm(film));
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
                () -> filmController.createFilm(film));
        assertEquals("Продолжительность должна быть положительным числом", exception.getMessage());
    }

    @Test
    void shouldPassValidationForReleaseDateExactlyOnMinimumDate() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120);

        Film created = filmController.createFilm(film);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void shouldPassValidationForDescriptionExactly200Characters() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(200, created.getDescription().length());
    }

    @Test
    void shouldPassValidationForDescriptionEmptyDescription() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription(null);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film created = filmController.createFilm(film);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertNull(created.getDescription());
    }

    @Test
    void shouldPassValidationForDurationExactlyOne() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(1);

        Film created = filmController.createFilm(film);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals(1, created.getDuration());
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2020, 5, 15));
        film.setDuration(120);

        Film created = filmController.createFilm(film);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("Test Film", created.getName());
        assertEquals("Valid description", created.getDescription());
        assertEquals(LocalDate.of(2020, 5, 15), created.getReleaseDate());
        assertEquals(120, created.getDuration());
    }
}