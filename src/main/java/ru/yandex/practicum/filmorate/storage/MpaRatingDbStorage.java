package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final JdbcTemplate jdbcTemplate;

    private RowMapper<MpaRating> mpaRatingRowMapper() {
        return (rs, rowNum) -> {
            MpaRating mpaRating = new MpaRating();
            mpaRating.setId(rs.getInt("id"));
            mpaRating.setName(rs.getString("name"));
            mpaRating.setDescription(rs.getString("description"));
            return mpaRating;
        };
    }

    public List<MpaRating> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaRatingRowMapper());
    }

    public Optional<MpaRating> getById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            MpaRating mpaRating = jdbcTemplate.queryForObject(sql, mpaRatingRowMapper(), id);
            return Optional.ofNullable(mpaRating);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}