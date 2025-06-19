package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();


    @Override
    public Collection<User> getUserList() {
        log.info("Получение списка пользователей.");
        return users.values();
    }

    @Override
    public User addNewUser(User user) {
        validateUser(user);
        user.setId(getNextUserId());
        user.setDefaultNameIfEmpty(user.getLogin());
        users.put(user.getId(), user);
        log.info("Новый пользователь с идентификатором {} зарегистрирован.", user.getId());
        return user;
    }

    @Override
    public User updateUserInfo(User updatedUser) {
        if (!users.containsKey(updatedUser.getId())) {
            log.error("При обновлении информации о пользователе, пользователь с id {} не найден.", updatedUser.getId());
            throw new NotFoundException("Пользователь с ID=" + updatedUser.getId() + " не существует.");
        }
        User oldUser = users.get(updatedUser.getId());
        validateUser(updatedUser);
        oldUser.updateFrom(updatedUser);
        log.info("Обновление профиля пользователя с идентификатором {}.", updatedUser.getId());
        return oldUser;
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            log.error("При удалении пользователя, пользователь с id {} не найден.", userId);
            throw new NotFoundException("Пользователь с ID=" + userId + " не существует.");
        }
        users.remove(userId);
        log.info("Удаление пользователя с идентификатором {}.", userId);
    }

    @Override
    public User findUserById(Long userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id {} не найден.", userId);
            throw new NotFoundException(" Пользователь с id = " + userId + " не найден.");
        }
        return users.get(userId);
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Ошибка валидации Логина.");
            throw new ValidationException("Логин не должен содержать пробелов!");
        }
        if (user.getBirthday().isAfter(java.time.LocalDate.now())) {
            log.error("Ошибка валидации Даты рождения.");
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