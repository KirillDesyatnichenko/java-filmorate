package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

@Repository
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<MpaRating> ratingRowMapper;

    private static final String GET_RATING_LIST_SQL = """
        SELECT *
        FROM rating
        """;

    private static final String FIND_RATING_BY_ID_SQL = """
        SELECT *
        FROM rating
        WHERE rating_id = ?
        """;

    public RatingDbStorage(JdbcTemplate jdbcTemplate, RowMapper<MpaRating> ratingRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.ratingRowMapper = ratingRowMapper;
    }

    @Override
    public Collection<MpaRating> getRatingList() {
        return jdbcTemplate.query(GET_RATING_LIST_SQL, ratingRowMapper);
    }

    @Override
    public Optional<MpaRating> getRatingById(int ratingId) {
        try {
            MpaRating rating = jdbcTemplate.queryForObject(FIND_RATING_BY_ID_SQL, ratingRowMapper, ratingId);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}