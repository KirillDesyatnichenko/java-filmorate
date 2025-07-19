package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class GenreDbStorage implements GenreStorage {

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
            """;

    private static final String GET_GENRES_FOR_FILMS_SQL_TEMPLATE = """
            SELECT fg.film_id, g.genre_id, g.genre_name
            FROM film_genre fg
            JOIN genre g ON fg.genre_id = g.genre_id
            WHERE fg.film_id IN (%s)
            """;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Genre> genreRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Collection<Genre> getGenreList() {
        return jdbcTemplate.query(GET_GENRE_LIST_SQL, genreRowMapper);
    }

    @Override
    public Optional<Genre> findGenreById(int genreId) {
        try {
            Genre genre = jdbcTemplate.queryForObject(FIND_GENRE_BY_ID_SQL, genreRowMapper, genreId);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Map<Long, Set<Genre>> getGenresForFilms(Set<Long> filmIds, Map<Long, Film> filmMap) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String inSql = filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        String finalSql = String.format(GET_GENRES_FOR_FILMS_SQL_TEMPLATE, inSql);

        jdbcTemplate.query(finalSql, rs -> {
            long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));

            filmMap.computeIfAbsent(filmId, id -> new Film())
                    .getGenres()
                    .add(genre);
        });

        return filmMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getGenres() != null
                                ? e.getValue().getGenres()
                                : new LinkedHashSet<>()
                ));
    }

    @Override
    public List<Genre> getGenresByFilmId(Long filmId) {
        return jdbcTemplate.query(GET_GENRES_BY_FILM_ID_SQL, genreRowMapper, filmId);
    }
}