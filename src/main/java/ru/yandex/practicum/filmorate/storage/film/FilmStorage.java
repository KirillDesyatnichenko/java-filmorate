package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Collection<Film> getFilmList();

    Film addNewFilm(Film film);

    Film updateFilmInfo(Film updatedFilm);

    void deleteFilm(Long filmId);

    Film findById(Long filmId);

    List<Film> getTopRatedFilms(int limit);
}