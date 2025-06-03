package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserModelValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testInvalidEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("Ошибочный Мейл");
        user.setLogin("Логин");
        user.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        boolean hasError = false;
        for (ConstraintViolation<User> violation : violations) {
            if ("Некорректный адрес электронной почты.".equals(violation.getMessage())) {
                hasError = true;
                break;
            }
        }
        assertTrue(hasError);
    }

    @Test
    public void testLoginContainsSpaces() {
        User user = new User();
        user.setId(1L);
        user.setEmail("mail@mail.ru");
        user.setLogin(null);
        user.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        boolean hasError = false;
        for (ConstraintViolation<User> violation : violations) {
            System.out.println(violation.getMessage());
            if ("Логин должен быть заполнен и не содержать пробелов!".equals(violation.getMessage())) {
                hasError = true;
                break;
            }
        }
        assertTrue(hasError);
    }
}