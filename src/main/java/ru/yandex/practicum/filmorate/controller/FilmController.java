package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;
    private final GenreDbStorage genreStorage;
    private final MpaRatingDbStorage mpaRatingStorage;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        validateFilm(film);
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        validateFilm(film);
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        log.info("Получен запрос на получение фильма с id: {}", id);
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на добавление лайка фильму {} от пользователя {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос на удаление лайка у фильма {} от пользователя {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        return filmService.getPopularFilms(count);
    }

    // НОВЫЕ ЭНДПОИНТЫ ДЛЯ ЖАНРОВ
    @GetMapping("/genres")
    public List<ru.yandex.practicum.filmorate.model.Genre> getAllGenres() {
        log.info("Получен запрос на получение всех жанров");
        return genreStorage.getAll();
    }

    @GetMapping("/genres/{id}")
    public ru.yandex.practicum.filmorate.model.Genre getGenreById(@PathVariable int id) {
        log.info("Получен запрос на получение жанра с id: {}", id);
        return genreStorage.getById(id)
                .orElseThrow(() -> {
                    log.error("Жанр с id {} не найден", id);
                    return new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр с id " + id + " не найден");
                });
    }

    // НОВЫЕ ЭНДПОИНТЫ ДЛЯ РЕЙТИНГОВ
    @GetMapping("/mpa")
    public List<ru.yandex.practicum.filmorate.model.MpaRating> getAllMpaRatings() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        return mpaRatingStorage.getAll();
    }

    @GetMapping("/mpa/{id}")
    public ru.yandex.practicum.filmorate.model.MpaRating getMpaRatingById(@PathVariable int id) {
        log.info("Получен запрос на получение рейтинга MPA с id: {}", id);
        return mpaRatingStorage.getById(id)
                .orElseThrow(() -> {
                    log.error("Рейтинг MPA с id {} не найден", id);
                    return new ru.yandex.practicum.filmorate.exception.NotFoundException("Рейтинг MPA с id " + id + " не найден");
                });
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().trim().isEmpty()) {
            log.error("Название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }
}
