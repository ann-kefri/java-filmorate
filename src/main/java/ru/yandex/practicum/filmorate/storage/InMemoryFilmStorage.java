package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.debug("Фильм сохранен в хранилище: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        log.debug("Фильм обновлен в хранилище: {}", film);
        return film;
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void delete(int id) {
        films.remove(id);
        log.debug("Фильм с id {} удален из хранилища", id);
    }
}