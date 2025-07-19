package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

public interface GenreStorage {
    Collection<Genre> getGenreList();

    Optional<Genre> findGenreById(int genreId);

    Map<Long, Set<Genre>> getGenresForFilms(Set<Long> filmIds, Map<Long, Film> filmMap);

    List<Genre> getGenresByFilmId(Long filmId);
}
