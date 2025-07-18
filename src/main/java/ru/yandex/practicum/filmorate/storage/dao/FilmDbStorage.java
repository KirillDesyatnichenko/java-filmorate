package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
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
    private final FilmRowMapper filmRowMapper;
    private final GenreDbStorage genreDbStorage;
    private final RatingDbStorage ratingDbStorage;

    @Override
    public Film addNewFilm(Film film) {
        ratingDbStorage.getRatingById(film.getMpa().getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreDbStorage.findGenreById(genre.getId());
            }
        }

        SimpleJdbcInsert insertFilm = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> params = new HashMap<>();
        params.put("title", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration_min", film.getDuration());
        params.put("rating_id", film.getMpa().getId());

        Number key = insertFilm.executeAndReturnKey(params);
        film.setId(key.longValue());

        saveGenres(film);
        return findById(film.getId());
    }

    @Override
    public Film updateFilmInfo(Film film) {
        ratingDbStorage.getRatingById(film.getMpa().getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreDbStorage.findGenreById(genre.getId());
            }
        }

        int updated = jdbcTemplate.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден.");
        }

        saveGenres(film);
        return findById(film.getId());
    }

    @Override
    public void deleteFilm(Long id) {
        int deleted = jdbcTemplate.update(SQL_DELETE_FILM, id);
        if (deleted == 0) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
    }

    @Override
    public Film findById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(SQL_FIND_FILM_BY_ID, filmRowMapper, id);
            assert film != null;
            enrichFilm(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
    }

    @Override
    public List<Film> getFilmList() {
        List<Film> films = jdbcTemplate.query(SQL_GET_ALL_FILMS, filmRowMapper);
        for (Film film : films) {
            enrichFilm(film);
        }
        return films;
    }

    @Override
    public List<Film> getTopRatedFilms(int limit) {
        List<Film> films = jdbcTemplate.query(SQL_GET_TOP_FILMS, filmRowMapper, limit);
        for (Film film : films) {
            film.setGenres(new HashSet<>(genreDbStorage.getGenresByFilmId(film.getId())));
        }
        return films;
    }

    private void enrichFilm(Film film) {
        Collection<Genre> genres = genreDbStorage.getGenresByFilmId(film.getId());
        film.setGenres(genres.stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        if (film.getMpa() == null) {
            throw new NotFoundException("У фильма не задан рейтинг (mpa).");
        }

        MpaRating rating = ratingDbStorage.getRatingById(film.getMpa().getId());
        film.setMpa(rating);
    }

    private void saveGenres(Film film) {
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, film.getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(SQL_INSERT_FILM_GENRE, film.getId(), genre.getId());
            }
        }
    }
}
