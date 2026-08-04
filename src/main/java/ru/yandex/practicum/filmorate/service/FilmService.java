package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film createFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        // Проверяем, существует ли фильм
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

    public Film addLike(int filmId, int userId) {
        // Проверяем, что пользователь существует
        if (userStorage.getById(userId).isEmpty()) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", filmId);
                    return new NotFoundException("Фильм с id " + filmId + " не найден");
                });

        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        return filmStorage.update(film);
    }

    public Film removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", filmId);
                    return new NotFoundException("Фильм с id " + filmId + " не найден");
                });

        if (!film.getLikes().remove(userId)) {
            log.warn("Пользователь {} не ставил лайк фильму {}", userId, filmId);
        }
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
        return filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}