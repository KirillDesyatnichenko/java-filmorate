package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage storage;

    public UserService(@Qualifier("userDbStorage") UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> getUserList() {
        return storage.getUserList();
    }

    public User getUserById(Long id) {
        return storage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
    }

    public User addNewUser(User user) {
        validateUser(user);
        user.setDefaultNameIfEmpty(user.getLogin());
        return storage.addNewUser(user);
    }

    public User updateUserInfo(User updatedUser) {
        if (updatedUser.getId() == null) {
            throw new ValidationException("Id пользователя должен быть указан при обновлении.");
        }

        if (storage.isUserNotExists(updatedUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + updatedUser.getId() + " не найден.");
        }

        updatedUser.setDefaultNameIfEmpty(updatedUser.getLogin());
        validateUser(updatedUser);
        return storage.updateUserInfo(updatedUser);
    }

    public void deleteUser(Long userId) {
        if (storage.isUserNotExists(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        storage.deleteUser(userId);
    }

    public void addFriend(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }

        getUserById(id);
        getUserById(friendId);

        storage.addFriend(id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей.");
        }

        getUserById(id);
        getUserById(friendId);

        storage.deleteFriend(id, friendId);
    }

    public List<User> getUserFriendList(Long id) {
        User user = getUserById(id);
        return storage.findUsersByIds(user.getFriends());
    }

    public List<User> getGeneralFriendList(Long id, Long otherId) {
        User user1 = getUserById(id);
        User user2 = getUserById(otherId);

        Set<Long> common = new HashSet<>(user1.getFriends());
        common.retainAll(user2.getFriends());

        return storage.findUsersByIds(common);
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелов.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }
}