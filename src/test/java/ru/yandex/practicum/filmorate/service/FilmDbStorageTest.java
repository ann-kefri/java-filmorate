package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.mappers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class,
        UserRowMapper.class, GenreDbStorage.class, MpaRatingDbStorage.class,
        GenreRowMapper.class, MpaRatingRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaRatingDbStorage mpaRatingStorage;

    @Test
    void testFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setGenres(Set.of(genreStorage.getById(1).orElseThrow()));
        film.setMpa(mpaRatingStorage.getById(3).orElseThrow());  // ← setMpa
        Film created = filmStorage.create(film);

        Optional<Film> filmOptional = filmStorage.getById(created.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", created.getId())
                );
    }

    @Test
    void testCreateFilm() {
        Film film = new Film();
        film.setName("New Film");
        film.setDescription("New Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setGenres(Set.of(genreStorage.getById(1).orElseThrow()));
        film.setMpa(mpaRatingStorage.getById(3).orElseThrow());  // ← setMpa

        Film created = filmStorage.create(film);

        assertThat(created)
                .hasFieldOrPropertyWithValue("name", "New Film")
                .hasFieldOrPropertyWithValue("description", "New Description")
                .hasFieldOrPropertyWithValue("duration", 120);
        assertThat(created.getId()).isPositive();
        assertThat(created.getGenres()).isNotEmpty();
        assertThat(created.getMpa()).isNotNull();  // ← getMpa
        assertThat(created.getMpa().getId()).isEqualTo(3);
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Update Film");
        film.setDescription("Update Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setGenres(Set.of(genreStorage.getById(1).orElseThrow()));
        film.setMpa(mpaRatingStorage.getById(3).orElseThrow());  // ← setMpa
        Film created = filmStorage.create(film);

        created.setName("Updated Film");
        created.setDescription("Updated Description");
        Film updated = filmStorage.update(created);

        assertThat(updated)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "Updated Film")
                .hasFieldOrPropertyWithValue("description", "Updated Description");
    }

    @Test
    void testGetAllFilms() {
        Film film = new Film();
        film.setName("All Film");
        film.setDescription("All Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        filmStorage.create(film);

        List<Film> films = filmStorage.getAll();

        assertThat(films).isNotEmpty();
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Delete Film");
        film.setDescription("Delete Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = filmStorage.create(film);

        filmStorage.delete(created.getId());
        Optional<Film> found = filmStorage.getById(created.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void testAddLike() {
        Film film = new Film();
        film.setName("Like Film");
        film.setDescription("Like Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = filmStorage.create(film);

        User user = new User();
        user.setEmail("like@test.com");
        user.setLogin("likelogin");
        user.setName("Like User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        filmStorage.addLike(created.getId(), createdUser.getId());

        Film likedFilm = filmStorage.getById(created.getId()).orElseThrow();
        assertThat(likedFilm.getLikes()).contains(createdUser.getId());
    }

    @Test
    void testRemoveLike() {
        Film film = new Film();
        film.setName("Remove Like Film");
        film.setDescription("Remove Like Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Film created = filmStorage.create(film);

        User user = new User();
        user.setEmail("removelike@test.com");
        user.setLogin("removelikelogin");
        user.setName("Remove Like User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        filmStorage.addLike(created.getId(), createdUser.getId());
        filmStorage.removeLike(created.getId(), createdUser.getId());

        Film filmAfterRemove = filmStorage.getById(created.getId()).orElseThrow();
        assertThat(filmAfterRemove.getLikes()).doesNotContain(createdUser.getId());
    }

    @Test
    void testGetPopularFilms() {
        Film film1 = new Film();
        film1.setName("Popular Film 1");
        film1.setDescription("Popular Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        Film created1 = filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Popular Film 2");
        film2.setDescription("Popular Description 2");
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDuration(100);
        Film created2 = filmStorage.create(film2);

        User user = new User();
        user.setEmail("popular@test.com");
        user.setLogin("popularlogin");
        user.setName("Popular User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        filmStorage.addLike(created1.getId(), createdUser.getId());

        List<Film> popular = filmStorage.getPopular(10);
        assertThat(popular).isNotEmpty();
    }
}