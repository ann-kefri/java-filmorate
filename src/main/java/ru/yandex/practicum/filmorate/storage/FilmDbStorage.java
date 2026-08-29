package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreDbStorage genreStorage;
    private final MpaRatingDbStorage mpaRatingStorage;

    private Map<Integer, Set<Genre>> getGenresByFilmIds(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT fg.film_id, g.* FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY g.id";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Map<Integer, Set<Genre>> genresMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            int filmId = (int) row.get("film_id");
            Genre genre = new Genre();
            genre.setId((int) row.get("id"));
            genre.setName((String) row.get("name"));

            genresMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        }

        return genresMap;
    }

    private Map<Integer, Set<Integer>> getLikesByFilmIds(List<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN (" + placeholders + ")";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Map<Integer, Set<Integer>> likesMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            int filmId = (int) row.get("film_id");
            int userId = (int) row.get("user_id");
            likesMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }

        return likesMap;
    }

    private void enrichFilmsWithGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Integer, Set<Genre>> genresMap = getGenresByFilmIds(filmIds);
        Map<Integer, Set<Integer>> likesMap = getLikesByFilmIds(filmIds);

        for (Film film : films) {
            film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>()));
            film.setLikes(likesMap.getOrDefault(film.getId(), new HashSet<>()));
        }
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        saveGenres(film);

        log.debug("Создан фильм в БД: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        if (rowsAffected == 0) {
            log.error("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        updateGenres(film);

        log.debug("Обновлен фильм в БД: {}", film);
        return film;
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            if (film.getMpa() != null) {
                mpaRatingStorage.getById(film.getMpa().getId())
                        .ifPresent(film::setMpa);
            }
        }

        enrichFilmsWithGenresAndLikes(films);

        return films;
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film != null) {
                if (film.getMpa() != null) {
                    mpaRatingStorage.getById(film.getMpa().getId())
                            .ifPresent(film::setMpa);
                }

                film.setGenres(new HashSet<>(genreStorage.getGenresByFilmId(film.getId())));

                String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
                List<Integer> likes = jdbcTemplate.query(likesSql,
                        (rs, rowNum) -> rs.getInt("user_id"),
                        film.getId());
                film.setLikes(new HashSet<>(likes));
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Фильм с id {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.debug("Фильм с id {} удален из БД", id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, COUNT(l.user_id) as likes_count FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);

        for (Film film : films) {
            if (film.getMpa() != null) {
                mpaRatingStorage.getById(film.getMpa().getId())
                        .ifPresent(film::setMpa);
            }
        }

        enrichFilmsWithGenresAndLikes(films);

        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    private void updateGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());
        saveGenres(film);
    }
}