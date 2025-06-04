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
        log.info("Получение списка юзеров.");
        return users.values();
    }

    @PostMapping
    public User addNewUser(@Valid @RequestBody User user) throws ValidationException {
        validateUser(user);
        user.setId(getNextUserId());
        user.setDefaultNameIfEmpty(user.getLogin());
        users.put(user.getId(), user);
        log.info("Новый пользователь с идентификатором {} зарегистрирован.", user.getId());
        return user;
    }

    @PutMapping
    public User updateUserInfo(@Valid @RequestBody User updatedUser) throws ValidationException {
        if (!users.containsKey(updatedUser.getId())) {
            log.error("Пользователь с id {} не найден.", updatedUser.getId());
            throw new NotFoundException("Пользователь с ID=" + updatedUser.getId() + " не существует.");
        }
        User oldUser = users.get(updatedUser.getId());
        validateUser(updatedUser);
        oldUser.updateFrom(updatedUser);
        log.info("Профиль пользователя с идентификатором {} успешно обновлён.", updatedUser.getId());
        return oldUser;
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации Логина");
            throw new ValidationException("Логин должен не содержать пробелов!");
        }
        if (user.getBirthday().isAfter(java.time.LocalDate.now())) {
            log.error("Ошибка валидации Даты рождения");
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