package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage storage;

    public Collection<Genre> getGenreList() {
        log.info("Получение списка всех жанров.");
        return storage.getGenreList();
    }

    public Genre findGenreById(int genreId) {
        log.info("Получение жанра с идентификатором {}", genreId);
        return storage.findGenreById(genreId);
    }
}