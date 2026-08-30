package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaRatingDbStorage mpaRatingStorage;

    public Film createFilm(Film film) {
        if (film.getMpa() != null) {
            mpaRatingStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> {
                        log.error("Рейтинг MPA с id {} не найден", film.getMpa().getId());
                        return new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
                    });
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreStorage.getById(genre.getId())
                        .orElseThrow(() -> {
                            log.error("Жанр с id {} не найден", genre.getId());
                            return new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                        });
            }
        }

        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        filmStorage.getById(film.getId())
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", film.getId());
                    return new NotFoundException("Фильм с id " + film.getId() + " не найден");
                });
        return filmStorage.update(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", id);
                    return new NotFoundException("Фильм с id " + id + " не найден");
                });
    }

    public void addLike(int filmId, int userId) {
        filmStorage.getById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", filmId);
                    return new NotFoundException("Фильм с id " + filmId + " не найден");
                });

        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.getById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", filmId);
                    return new NotFoundException("Фильм с id " + filmId + " не найден");
                });

        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id {} не найден", userId);
                    return new NotFoundException("Пользователь с id " + userId + " не найден");
                });

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopular(count);
    }
}