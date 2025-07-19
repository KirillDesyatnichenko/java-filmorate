package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikeStorage;
import ru.yandex.practicum.filmorate.storage.dao.RatingStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.IllegalArgumentException;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate FIRST_FILM_RELEASE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeStorage likeStorage;
    private final GenreStorage genreStorage;
    private final RatingStorage ratingStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikeStorage likeStorage,
                       GenreStorage genreStorage,
                       RatingStorage ratingDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeStorage = likeStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingDbStorage;
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .map(this::enrichFilm)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден."));
    }

    public Collection<Film> getFilmList() {
        Collection<Film> films = filmStorage.getFilmList();

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));
        Set<Long> filmIds = filmMap.keySet();

        Map<Long, Set<Genre>> genreMap = genreStorage.getGenresForFilms(filmIds, filmMap);

        for (Film film : films) {
            film.setGenres(genreMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }

        return films;
    }

    public Film addNewFilm(Film film) {
        validateFilm(film);

        film.setMpa(getRatingOrThrow(film.getMpa().getId()));

        if (film.getGenres() != null) {
            Set<Genre> validatedGenres = film.getGenres().stream()
                    .map(g -> getGenreOrThrow(g.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(validatedGenres);
        }

        Film saved = filmStorage.addNewFilm(film);
        film.setId(saved.getId());
        filmStorage.saveGenres(film);
        return findById(saved.getId());
    }

    public Film updateFilmInfo(Film film) {
        validateFilm(film);

        film.setMpa(getRatingOrThrow(film.getMpa().getId()));

        if (film.getGenres() != null) {
            Set<Genre> validatedGenres = film.getGenres().stream()
                    .map(g -> getGenreOrThrow(g.getId()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(validatedGenres);
        }

        Film updated = filmStorage.updateFilmInfo(film);
        if (updated == null) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден.");
        }

        filmStorage.saveGenres(film);
        return findById(film.getId());
    }

    public void deleteFilm(Long id) {
        if (filmStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        filmStorage.deleteFilm(id);
    }

    public List<Film> getTopRatedFilms(int limit) {
        List<Film> films = filmStorage.getTopRatedFilms(limit);

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));
        Set<Long> filmIds = filmMap.keySet();

        Map<Long, Set<Genre>> genreMap = genreStorage.getGenresForFilms(filmIds, filmMap);

        for (Film film : films) {
            film.setGenres(genreMap.getOrDefault(film.getId(), new LinkedHashSet<>()));
        }

        return films;
    }

    public void likeFilm(Long filmId, Long userId) {
        findById(filmId);
        userStorage.findUserById(userId);

        if (likeStorage.hasLike(filmId, userId)) {
            throw new IllegalArgumentException("Вы уже поставили лайк фильму!");
        }

        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        findById(filmId);
        userStorage.findUserById(userId);

        if (!likeStorage.hasLike(filmId, userId)) {
            throw new IllegalArgumentException("У вас нет лайка на этом фильме!");
        }

        likeStorage.removeLike(filmId, userId);
    }

    private MpaRating getRatingOrThrow(int id) {
        return ratingStorage.getRatingById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + id + " не найден."));
    }

    private Genre getGenreOrThrow(int id) {
        return genreStorage.findGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден."));
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FIRST_FILM_RELEASE)) {
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895 года!");
        }
    }

    private Film enrichFilm(Film film) {
        Set<Genre> genres = new LinkedHashSet<>(genreStorage.getGenresByFilmId(film.getId()));
        film.setGenres(genres);
        int ratingId = film.getMpa().getId();
        MpaRating rating = ratingStorage.getRatingById(ratingId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + ratingId + " не найден."));

        film.setMpa(rating);
        return film;
    }
}