package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreStorage storage;

    public Collection<Genre> getGenreList() {
        log.info("Получение списка всех жанров.");
        return storage.getGenreList();
    }

    public Genre getGenreById(int id) {
        return storage.findGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден."));
    }
}