package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Repository
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> genreRowMapper;

    private static final String GET_GENRE_LIST_SQL = """
        SELECT *
        FROM genre
        """;

    private static final String FIND_GENRE_BY_ID_SQL = """
        SELECT *
        FROM genre
        WHERE genre_id = ?
        """;

    private static final String GET_GENRES_BY_FILM_ID_SQL = """
        SELECT g.genre_id, g.genre_name
        FROM film_genre fg
        JOIN genre g ON fg.genre_id = g.genre_id
        WHERE fg.film_id = ?
        ORDER BY g.genre_id
        """;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Genre> genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    public Collection<Genre> getGenreList() {
        return jdbcTemplate.query(GET_GENRE_LIST_SQL, genreRowMapper);
    }

    public Genre findGenreById(int genreId) {
        try {
            return jdbcTemplate.queryForObject(FIND_GENRE_BY_ID_SQL, genreRowMapper, genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с id " + genreId + " не найден.");
        }
    }

    public Collection<Genre> getGenresByFilmId(Long filmId) {
        return jdbcTemplate.query(GET_GENRES_BY_FILM_ID_SQL, genreRowMapper, filmId);
    }
}
