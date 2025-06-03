package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUserList() {
        return users.values();
    }

    @PostMapping
    public User addNewUser(@Valid @RequestBody User user) throws ValidationException {
        try {
            validateUser(user);
            user.setId(getNextUserId());
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Новый пользователь с идентификатором {} зарегистрирован.", user.getId());
            return user;
        } catch (ValidationException e) {
            log.error("Ошибка валидации при создании пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User updateUserInfo(@Valid @RequestBody User updatedUser) throws ValidationException {
        try {
            if (!users.containsKey(updatedUser.getId())) {
                throw new NotFoundException("Пользователь с ID=" + updatedUser.getId() + " не существует.");
            }
            User oldUser = users.get(updatedUser.getId());
            validateUser(updatedUser);
            oldUser.updateFrom(updatedUser);
            log.info("Профиль пользователя с идентификатором {} успешно обновлён.", updatedUser.getId());
            return oldUser;
        } catch (ValidationException | NotFoundException e) {
            log.error("Ошибка обновления пользователя с ID {}: {}", updatedUser.getId(), e.getMessage());
            throw e;
        }
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() ||
                !user.getEmail().contains("@")) {
            throw new ValidationException("Адрес электронной почты некорректен!");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() ||
                user.getLogin().contains(" ")) {
            throw new ValidationException("Логин должен быть заполнен и не содержать пробелов!");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }
    }

    private long getNextUserId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}