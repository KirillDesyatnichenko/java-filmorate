package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Long id;
    @NotBlank(message = "Название фильма обязательно!")
    private String name;
    @Size(max = 200, message = "Описание превышает максимальную длину в 200 символов!")
    private String description;
    @NotNull(message = "Дата релиза фильма не заполнена!")
    private LocalDate releaseDate;
    @NotNull(message = "Длительность фильма не заполнена!")
    @Positive(message = "Длительность фильма должна быть положительной величиной!")
    private Integer duration;

    private Set<Long> likes = new HashSet<>();

    public void updateFrom(Film updatedFilm) {
        if (updatedFilm.getName() != null && !updatedFilm.getName().isBlank()) {
            this.name = updatedFilm.getName();
        }
        if (updatedFilm.getDescription() != null && !updatedFilm.getDescription().isBlank()) {
            this.description = updatedFilm.getDescription();
        }
        if (updatedFilm.getReleaseDate() != null) {
            this.releaseDate = updatedFilm.getReleaseDate();
        }
        if (updatedFilm.getDuration() != null) {
            this.duration = updatedFilm.getDuration();
        }
    }
}