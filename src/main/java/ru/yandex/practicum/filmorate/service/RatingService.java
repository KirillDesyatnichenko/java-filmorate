package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.dao.RatingStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {

    private final RatingStorage storage;

    public Collection<MpaRating> getRatingList() {
        log.info("Получение списка всех рейтингов.");
        return storage.getRatingList();
    }

    public MpaRating findRatingById(int ratingId) {
        return storage.getRatingById(ratingId)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id " + ratingId + " не найден."));
    }
}