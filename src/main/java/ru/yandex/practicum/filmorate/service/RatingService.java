package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.RatingDbStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingDbStorage storage;

    public Collection<MpaRating> getRatingList() {
        log.info("Получение списка всех рейтингов.");
        return storage.getRatingList();
    }

    public MpaRating findRatingById(int ratingId) {
        log.info("Получение рейтинга с идентификатором {}", ratingId);
        return storage.getRatingById(ratingId);
    }
}