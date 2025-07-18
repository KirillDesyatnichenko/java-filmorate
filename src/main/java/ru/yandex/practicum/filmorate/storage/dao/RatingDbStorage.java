package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

@Repository
public class RatingDbStorage {

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

    public Collection<MpaRating> getRatingList() {
        return jdbcTemplate.query(GET_RATING_LIST_SQL, ratingRowMapper);
    }

    public MpaRating getRatingById(int ratingId) {
        try {
            return jdbcTemplate.queryForObject(FIND_RATING_BY_ID_SQL, ratingRowMapper, ratingId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг с id " + ratingId + " не найден.");
        }
    }
}
