package ru.yandex.practicum.filmorate.storage.dao;

import java.util.Set;

public interface LikeStorage {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    boolean hasLike(Long filmId, Long userId);

    Set<Long> getLikesForFilm(Long filmId);
}
