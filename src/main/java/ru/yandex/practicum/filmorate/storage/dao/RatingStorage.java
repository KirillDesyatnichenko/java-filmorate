package ru.yandex.practicum.filmorate.storage.dao;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.Optional;

public interface RatingStorage {
    Collection<MpaRating> getRatingList();

    Optional<MpaRating> getRatingById(int ratingId);
}
