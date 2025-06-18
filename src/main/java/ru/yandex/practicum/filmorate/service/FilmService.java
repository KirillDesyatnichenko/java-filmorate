package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.IllegalArgumentException;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage storage;
    private final UserStorage userStorage;

    public void likeFilm(Long filmId, Long userId) {
        Film film = storage.findById(filmId);
        userStorage.findUserById(userId);
        if (film.getLikes().contains(userId)) {
            log.warn("Пользователь с id {} уже ставил лайк этому фильму (ID {}) ранее.", userId, filmId);
            throw new IllegalArgumentException("Вы уже поставили лайк данному фильму!");
        }

        film.getLikes().add(userId);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
    }

    public void removeLike(Long id, Long userId) {
        Film film = storage.findById(id);
        userStorage.findUserById(userId);
        if (!film.getLikes().contains(userId)) {
            log.warn("У пользователя с id {} нет лайка на этот фильм (ID {}). Лайк нельзя снять.", userId, id);
            throw new IllegalArgumentException("У вас нет лайка на данном фильме!");
        }

        film.getLikes().remove(userId);
        log.info("Пользователь с id {} снял лайк с фильма с id {}", userId, id);
    }

    public List<Film> getTopRatedFilms(int limit) {
        Collection<Film> allFilms = storage.getFilmList();
        log.info("Получение списка из {} фильмов с наибольшим количеством лайков.", limit);
        return allFilms.stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}