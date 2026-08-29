package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;
    private InMemoryFilmStorage filmStorage;
    private InMemoryUserStorage userStorage;
    private GenreDbStorage genreStorage;
    private MpaRatingDbStorage mpaRatingStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        genreStorage = new GenreDbStorage(null, null);
        mpaRatingStorage = new MpaRatingDbStorage(null, null);
        filmService = new FilmService(filmStorage, userStorage, genreStorage, mpaRatingStorage);
        userService = new UserService(userStorage);
    }

    @Test
    void shouldAddLike() {
        Film film = createTestFilm();
        User user = createTestUser();

        Film savedFilm = filmService.createFilm(film);
        User savedUser = userService.createUser(user);

        filmService.addLike(savedFilm.getId(), savedUser.getId());

        Film likedFilm = filmService.getFilmById(savedFilm.getId());
        assertTrue(likedFilm.getLikes().contains(savedUser.getId()));
        assertEquals(1, likedFilm.getLikes().size());
    }

    @Test
    void shouldGetPopularFilms() {
        User user1 = createTestUser("user1@test.com", "user1");
        User user2 = createTestUser("user2@test.com", "user2");

        User savedUser1 = userService.createUser(user1);
        User savedUser2 = userService.createUser(user2);

        Film film1 = createTestFilm("Фильм 1");
        Film film2 = createTestFilm("Фильм 2");

        Film savedFilm1 = filmService.createFilm(film1);
        Film savedFilm2 = filmService.createFilm(film2);

        filmService.addLike(savedFilm1.getId(), savedUser1.getId());
        filmService.addLike(savedFilm1.getId(), savedUser2.getId());
        filmService.addLike(savedFilm2.getId(), savedUser1.getId());

        List<Film> popular = filmService.getPopularFilms(10);

        assertEquals(2, popular.get(0).getLikes().size());
        assertEquals(1, popular.get(1).getLikes().size());
        assertEquals(savedFilm1.getId(), popular.get(0).getId());
        assertEquals(savedFilm2.getId(), popular.get(1).getId());
    }

    @Test
    void shouldRemoveLike() {
        Film film = createTestFilm();
        User user = createTestUser();

        Film savedFilm = filmService.createFilm(film);
        User savedUser = userService.createUser(user);

        filmService.addLike(savedFilm.getId(), savedUser.getId());
        assertTrue(filmService.getFilmById(savedFilm.getId()).getLikes().contains(savedUser.getId()));

        filmService.removeLike(savedFilm.getId(), savedUser.getId());

        Film filmAfterRemove = filmService.getFilmById(savedFilm.getId());
        assertFalse(filmAfterRemove.getLikes().contains(savedUser.getId()));
        assertEquals(0, filmAfterRemove.getLikes().size());
    }

    @Test
    void shouldHandleDuplicateLike() {
        Film film = createTestFilm();
        User user = createTestUser();

        Film savedFilm = filmService.createFilm(film);
        User savedUser = userService.createUser(user);

        filmService.addLike(savedFilm.getId(), savedUser.getId());
        filmService.addLike(savedFilm.getId(), savedUser.getId());

        Film likedFilm = filmService.getFilmById(savedFilm.getId());
        assertEquals(1, likedFilm.getLikes().size());
        assertTrue(likedFilm.getLikes().contains(savedUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenLikeFromNonExistentUser() {
        Film film = createTestFilm();
        Film savedFilm = filmService.createFilm(film);

        assertThrows(NotFoundException.class,
                () -> filmService.addLike(savedFilm.getId(), 999));
    }

    @Test
    void shouldThrowExceptionWhenLikeNonExistentFilm() {
        User user = createTestUser();
        User savedUser = userService.createUser(user);

        assertThrows(NotFoundException.class,
                () -> filmService.addLike(999, savedUser.getId()));
    }

    @Test
    void shouldThrowExceptionWhenRemoveLikeFromNonExistentUser() {
        Film film = createTestFilm();
        Film savedFilm = filmService.createFilm(film);

        assertThrows(NotFoundException.class,
                () -> filmService.removeLike(savedFilm.getId(), 999));
    }

    @Test
    void shouldThrowExceptionWhenRemoveLikeNonExistentFilm() {
        User user = createTestUser();
        User savedUser = userService.createUser(user);

        assertThrows(NotFoundException.class,
                () -> filmService.removeLike(999, savedUser.getId()));
    }

    private Film createTestFilm() {
        return createTestFilm("Test Film");
    }

    private Film createTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    private User createTestUser() {
        return createTestUser("test@test.com", "testlogin");
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}