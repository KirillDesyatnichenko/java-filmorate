package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.dao.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.IllegalArgumentException;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate FIRST_FILM_RELEASE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeDbStorage likeDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikeDbStorage likeDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeDbStorage = likeDbStorage;
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }

    public Collection<Film> getFilmList() {
        return filmStorage.getFilmList();
    }

    public Film addNewFilm(Film film) throws ValidationException {
        validateFilm(film);
        return filmStorage.addNewFilm(film);
    }

    public Film updateFilmInfo(Film updatedFilm) throws ValidationException {
        validateFilm(updatedFilm);
        return filmStorage.updateFilmInfo(updatedFilm);
    }

    public void deleteFilm(Long filmId) {
        filmStorage.deleteFilm(filmId);
    }

    public List<Film> getTopRatedFilms(int count) {
        return filmStorage.getTopRatedFilms(count);
    }

    public void likeFilm(Long filmId, Long userId) {
        filmStorage.findById(filmId);       // валидация, выбросит NotFound
        userStorage.findUserById(userId);   // валидация, выбросит NotFound

        if (likeDbStorage.hasLike(filmId, userId)) {
            log.warn("Пользователь с id {} уже ставил лайк фильму {}", userId, filmId);
            throw new IllegalArgumentException("Вы уже поставили лайк фильму!");
        }

        likeDbStorage.addLike(filmId, userId);
        log.info("Пользователь с id {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.findById(filmId);
        userStorage.findUserById(userId);

        if (!likeDbStorage.hasLike(filmId, userId)) {
            log.warn("Лайк не найден: пользователь {} — фильм {}", userId, filmId);
            throw new IllegalArgumentException("У вас нет лайка на этом фильме!");
        }

        likeDbStorage.removeLike(filmId, userId);
        log.info("Пользователь {} снял лайк с фильма {}", userId, filmId);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(FIRST_FILM_RELEASE)) {
            throw new ValidationException("Дата релиза фильма не может быть раньше 28 декабря 1895 года!");
        }
    }
}