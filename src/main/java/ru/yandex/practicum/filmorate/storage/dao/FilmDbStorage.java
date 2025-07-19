package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@Slf4j
@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private static final String SQL_UPDATE_FILM = """
            UPDATE films SET title = ?, description = ?, release_date = ?, duration_min = ?, rating_id = ? WHERE film_id = ?
            """;

    private static final String SQL_DELETE_FILM = """
            DELETE FROM films WHERE film_id = ?
            """;

    private static final String SQL_FIND_FILM_BY_ID = """
            SELECT f.*, r.mpa_rating
            FROM films f
            JOIN rating r ON f.rating_id = r.rating_id
            WHERE f.film_id = ?
            """;

    private static final String SQL_GET_ALL_FILMS = """
            SELECT f.*, r.mpa_rating
            FROM films f
            JOIN rating r ON f.rating_id = r.rating_id
            """;

    private static final String SQL_GET_TOP_FILMS = """
            SELECT f.film_id,
                   f.title,
                   f.description,
                   f.release_date,
                   f.duration_min,
                   f.rating_id,
                   r.mpa_rating
            FROM films f
            LEFT JOIN likes l ON f.film_id = l.film_id
            LEFT JOIN rating r ON f.rating_id = r.rating_id
            GROUP BY f.film_id, f.title, f.description, f.release_date, f.duration_min, f.rating_id, r.mpa_rating
            ORDER BY COUNT(l.user_id) DESC, f.film_id ASC
            LIMIT ?
            """;

    private static final String SQL_DELETE_FILM_GENRES = """
            DELETE FROM film_genre WHERE film_id = ?
            """;

    private static final String SQL_INSERT_FILM_GENRE = """
            INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film>  filmRowMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public Film addNewFilm(Film film) {
        SimpleJdbcInsert insertFilm = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> params = Map.of(
                "title", film.getName(),
                "description", film.getDescription(),
                "release_date", film.getReleaseDate(),
                "duration_min", film.getDuration(),
                "rating_id", film.getMpa().getId()
        );

        Number key = insertFilm.executeAndReturnKey(params);
        film.setId(key.longValue());
        return film;
    }

    @Override
    public Film updateFilmInfo(Film film) {
        int updated = jdbcTemplate.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        return updated > 0 ? film : null;
    }

    @Override
    public void deleteFilm(Long id) {
        jdbcTemplate.update(SQL_DELETE_FILM, id);
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(SQL_FIND_FILM_BY_ID, filmRowMapper, id);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getFilmList() {
        return jdbcTemplate.query(SQL_GET_ALL_FILMS, filmRowMapper);
    }

    @Override
    public List<Film> getTopRatedFilms(int limit) {
        return jdbcTemplate.query(SQL_GET_TOP_FILMS, filmRowMapper, limit);
    }

    @Override
    public void saveGenres(Film film) {
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(SQL_INSERT_FILM_GENRE, film.getId(), genre.getId());
            }
        }
    }
}
