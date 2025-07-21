package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getFilmList();

    Film addNewFilm(Film film);

    Film updateFilmInfo(Film updatedFilm);

    void deleteFilm(Long filmId);

    Optional<Film> findById(Long filmId);

    List<Film> getTopRatedFilms(int limit);

    void saveGenres(Film film);
}