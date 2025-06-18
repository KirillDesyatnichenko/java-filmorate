package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();


    @Override
    public Collection<Film> getFilmList() {
        log.info("Получение списка фильмов.");
        return films.values();
    }

    @Override
    public Film addNewFilm(Film film) {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан новый фильм с идентификатором {}.", film.getId());
        return film;
    }

    @Override
    public Film updateFilmInfo(Film updatedFilm) {
        if (!films.containsKey(updatedFilm.getId())) {
            log.error("При обновлении информации о фильме, фильм с id {} не найден.", updatedFilm.getId());
            throw new NotFoundException("Фильм с id = " + updatedFilm.getId() + " не найден");
        }
        Film oldFilm = films.get(updatedFilm.getId());
        validateFilm(updatedFilm);
        oldFilm.updateFrom(updatedFilm);
        log.info("Информация о фильме с идентификатором {} успешно обновлена.", updatedFilm.getId());
        return oldFilm;
    }

    @Override
    public void deleteFilm(Long filmId) {
        if (!films.containsKey(filmId)) {
            log.error("При удалении фильма, фильм с id {} не найден.", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        films.remove(filmId);
        log.info("Удаление фильма с идентификатором {}.", filmId);
    }

    @Override
    public Film findById(Long filmId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильма с id {} не найден.", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        return films.get(filmId);
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(java.time.LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}