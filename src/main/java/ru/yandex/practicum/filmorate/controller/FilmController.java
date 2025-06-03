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
        return films.values();
    }

    @PostMapping
    public Film addNewFilm(@Valid @RequestBody Film film) throws ValidationException {
        try {
            validateFilm(film);
            film.setId(getNextId());
            films.put(film.getId(), film);
            log.info("Фильм успешно добавлен с идентификатором {}", film.getId());
            return film;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при добавлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public Film updateFilmInfo(@Valid @RequestBody Film updatedFilm) throws ValidationException {
        try {
            if (!films.containsKey(updatedFilm.getId())) {
                throw new NotFoundException("Фильм с id = " + updatedFilm.getId() + " не найден");
            }
            Film oldFilm = films.get(updatedFilm.getId());
            validateFilm(updatedFilm);
            oldFilm.updateFrom(updatedFilm);
            log.info("Информация о фильме с идентификатором {} успешно обновлена.", updatedFilm.getId());
            return oldFilm;
        } catch (ValidationException | NotFoundException e) {
            log.error("Ошибка обновления фильма с id {}: {}", updatedFilm.getId(), e.getMessage());
            throw e;
        }
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма должно быть указано!");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов!");
        }
        if (film.getReleaseDate().isBefore(java.time.LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть ранее 28 декабря 1895 года!");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Длительность фильма должна быть положительной величиной!");
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