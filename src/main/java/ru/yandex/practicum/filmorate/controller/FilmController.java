package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilmList() {
        log.info("Получение списка фильмов.");
        return films.values();
    }

    @PostMapping
    public Film addNewFilm(@Valid @RequestBody Film film) throws ValidationException {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен с идентификатором {}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilmInfo(@Valid @RequestBody Film updatedFilm) throws ValidationException {
        if (!films.containsKey(updatedFilm.getId())) {
            log.error("Фильм с id {} не найден", updatedFilm.getId());
            throw new NotFoundException("Фильм с id = " + updatedFilm.getId() + " не найден");
        }
        Film oldFilm = films.get(updatedFilm.getId());
        validateFilm(updatedFilm);
        oldFilm.updateFrom(updatedFilm);
        log.info("Информация о фильме с идентификатором {} успешно обновлена.", updatedFilm.getId());
        return oldFilm;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(java.time.LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть ранее 28 декабря 1895 года!");
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