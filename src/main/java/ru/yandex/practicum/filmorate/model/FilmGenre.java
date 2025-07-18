package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"id"})
public class FilmGenre {
    private Long id;
    private Long filmId;
    private int genreId;
}