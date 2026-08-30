package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRatingRowMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper mpaRatingRowMapper;

    public List<MpaRating> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaRatingRowMapper);
    }

    public Optional<MpaRating> getById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            MpaRating mpaRating = jdbcTemplate.queryForObject(sql, mpaRatingRowMapper, id);
            return Optional.ofNullable(mpaRating);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Рейтинг MPA с id {} не найден", id);
            return Optional.empty();
        }
    }
}