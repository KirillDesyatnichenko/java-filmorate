package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService service;
    private final FilmStorage storage;


    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("Запрошена информация о фильме с id={}", id);
        return storage.findById(id);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Запрошен полный список фильмов.");
        return storage.getFilmList();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) throws ValidationException {
        log.info("Создание нового фильма с названием '{}'", film.getName());
        return storage.addNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) throws ValidationException {
        log.info("Новая информация о фильме с идентификатором {} успешно добавлена.", updatedFilm.getId());
        return storage.updateFilmInfo(updatedFilm);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.info("Фильм с идентификатором {} успешно удалён.", id);
        storage.deleteFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с id={} добавляет лайк фильму с id={}", userId, id);
        service.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь с id={} снимает лайк с фильма с id={}", userId, id);
        service.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Отображается список {} самых популярных фильмов.", count);
        return service.getTopRatedFilms(count);
    }
}