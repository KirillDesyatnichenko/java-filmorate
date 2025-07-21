package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUserList() {
        log.info("Получение списка всех пользователей (InMemory).");
        return users.values();
    }

    @Override
    public User addNewUser(User user) {
        validateUser(user);
        user.setId(getNextUserId());
        user.setDefaultNameIfEmpty(user.getLogin());
        users.put(user.getId(), user);
        log.info("Пользователь создан: {} (InMemory)", user.getId());
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        Long id = user.getId();
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден.");
        }
        users.put(id, user);
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            log.error("Удаление пользователя: id {} не найден (InMemory).", userId);
            throw new NotFoundException("Пользователь с ID=" + userId + " не найден.");
        }
        users.remove(userId);
        log.info("Пользователь удален: {} (InMemory)", userId);
    }

    @Override
    public Optional<User> findUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            log.error("Пользователь не найден: id {} (InMemory)", userId);
            return Optional.empty();
        }
        return Optional.of(user);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        Optional<User> optUser = findUserById(userId);
        Optional<User> optFriend = findUserById(friendId);

        optUser.ifPresentOrElse(
                user -> optFriend.ifPresentOrElse(
                        friend -> {
                            if (user.getFriends().add(friendId)) {
                                log.info("Пользователь {} добавил в друзья пользователя {} (InMemory)", userId, friendId);
                            } else {
                                log.warn("Пользователь {} уже является другом пользователя {} (InMemory)", friendId, userId);
                            }
                        },
                        () -> log.error("Пользователь с id {} не найден.", friendId)
                ),
                () -> log.error("Пользователь с id {} не найден.", userId)
        );
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        Optional<User> optUser = findUserById(userId);
        Optional<User> optFriend = findUserById(friendId);

        optUser.ifPresentOrElse(
                user -> optFriend.ifPresentOrElse(
                        friend -> {
                            if (user.getFriends().remove(friendId)) {
                                log.info("Пользователь {} удалил из друзей пользователя {} (InMemory)", userId, friendId);
                            } else {
                                log.warn("Пользователь {} не был в друзьях у пользователя {} (InMemory)", friendId, userId);
                            }
                        },
                        () -> log.error("Пользователь с id {} не найден.", friendId)
                ),
                () -> log.error("Пользователь с id {} не найден.", userId)
        );
    }

    @Override
    public List<User> findUsersByIds(Collection<Long> ids) {
        return users.values().stream()
                .filter(user -> ids.contains(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUserNotExists(Long id) {
        return users.containsKey(id);
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            log.error("Ошибка валидации логина: '{}'", user.getLogin());
            throw new ValidationException("Логин не должен быть пустым и не должен содержать пробелов.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
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