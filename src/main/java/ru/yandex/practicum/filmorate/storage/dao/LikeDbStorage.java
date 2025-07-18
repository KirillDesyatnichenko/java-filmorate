package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
public class LikeDbStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_LIKE_SQL = """
        INSERT INTO likes (film_id, user_id)
        VALUES (?, ?)
        """;

    private static final String REMOVE_LIKE_SQL = """
        DELETE FROM likes
        WHERE film_id = ? AND user_id = ?
        """;

    private static final String HAS_LIKE_SQL = """
        SELECT COUNT(*)
        FROM likes
        WHERE film_id = ? AND user_id = ?
        """;

    private static final String GET_LIKES_FOR_FILM_SQL = """
        SELECT user_id
        FROM likes
        WHERE film_id = ?
        """;

    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(ADD_LIKE_SQL, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(REMOVE_LIKE_SQL, filmId, userId);
    }

    public boolean hasLike(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(HAS_LIKE_SQL, Integer.class, filmId, userId);
        return count != null && count > 0;
    }

    public Set<Long> getLikesForFilm(Long filmId) {
        return new HashSet<>(jdbcTemplate.queryForList(GET_LIKES_FOR_FILM_SQL, Long.class, filmId));
    }
}