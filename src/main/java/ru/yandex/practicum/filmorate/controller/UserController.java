package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserStorage storage;
    private final UserService service;

    @GetMapping
    public Collection<User> getUserList() {
        log.info("Запрошены все пользователи.");
        return storage.getUserList();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        log.info("Запрошена информация о пользователе с id={}", id);
        return storage.findUserById(id);
    }

    @PostMapping
    public User addNewUser(@Valid @RequestBody User user) throws ValidationException {
        log.info("Регистрация нового пользователя с именем '{}'.", user.getName());
        return storage.addNewUser(user);
    }

    @PutMapping
    public User updateUserInfo(@Valid @RequestBody User updatedUser) throws ValidationException {
        log.info("Профиль пользователя с идентификатором {} успешно обновлён.", updatedUser.getId());
        return storage.updateUserInfo(updatedUser);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя с идентификатором {}.", userId);
        storage.deleteUser(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Пользователь с идентификатором {} добавил в друзья пользователя с идентификатором {}.", id, friendId);
        service.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Пользователь с идентификатором {} удалил из друзей пользователя с идентификатором {}.", id, friendId);
        service.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriendList(@PathVariable Long id) {
        log.info("Отображается список друзей пользователя с id={}", id);
        return service.getUserFriendList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getGeneralFriendList(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Отображается список общих друзей пользователей с id={} и id={}", id, otherId);
        return service.getGeneralFriendList(id, otherId);
    }
}