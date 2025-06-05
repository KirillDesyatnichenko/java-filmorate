package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmModelValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testNullOrEmptyName() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("a");
        film.setReleaseDate(LocalDate.of(2000, 12, 27));
        film.setDuration(100);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        boolean hasError = false;
        for (ConstraintViolation<Film> violation : violations) {
            if ("Название фильма обязательно!".equals(violation.getMessage())) {
                hasError = true;
                break;
            }
        }
        assertTrue(hasError);
    }

    @Test
    public void testDescriptionTooLong() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 12, 27));
        film.setDuration(100);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        boolean hasError = false;
        for (ConstraintViolation<Film> violation : violations) {
            if ("Описание превышает максимальную длину в 200 символов!".equals(violation.getMessage())) {
                hasError = true;
                break;
            }
        }
        assertTrue(hasError);
    }

    @Test
    public void testNegativeDuration() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a");
        film.setReleaseDate(LocalDate.of(2000, 12, 27));
        film.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        boolean hasError = false;
        for (ConstraintViolation<Film> violation : violations) {
            if ("Длительность фильма должна быть положительной величиной!".equals(violation.getMessage())) {
                hasError = true;
                break;
            }
        }
        assertTrue(hasError);
    }
}