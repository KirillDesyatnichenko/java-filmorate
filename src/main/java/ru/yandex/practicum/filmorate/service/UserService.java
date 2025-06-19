package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage storage;

    public Collection<User> getUserList() {
        return storage.getUserList();
    }

    public User getUserById(Long id) {
        return storage.findUserById(id);
    }

    public User addNewUser(User user) throws ValidationException {
        return storage.addNewUser(user);
    }

    public User updateUserInfo(User updatedUser) throws ValidationException {
        return storage.updateUserInfo(updatedUser);
    }

    public void deleteUser(Long userId) {
        storage.deleteUser(userId);
    }

    public void addFriend(Long id, Long friendId) {
        User user = storage.findUserById(id);
        User friend = storage.findUserById(friendId);

        if (!user.getFriends().contains(friendId) && !friend.getFriends().contains(id)) {
            user.getFriends().add(friendId);
            friend.getFriends().add(id);
            log.info("Пользователь с идентификатором {} добавил в друзья пользователя с идентификатором {}", id, friendId);
        } else {
            log.warn("Пользователь с идентификаторами {} и {} уже являются друзьями.", friendId, id);
        }
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = storage.findUserById(id);
        User friend = storage.findUserById(friendId);

        if (user.getFriends().contains(friendId) && friend.getFriends().contains(id)) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(id);
            log.info("Пользователь с идентификатором {} удалил из друзей пользователя с идентификатором {}", id, friendId);
        } else {
            log.warn("Пользователи {} и {} отсутствуют в списках друзей друг друга.", friendId, id);
        }
    }

    public List<User> getUserFriendList(Long id) throws NotFoundException {
        User user = storage.findUserById(id);
        Set<Long> friendsIds = user.getFriends();
        List<User> result = new ArrayList<>(friendsIds.size());

        for (Long friendId : friendsIds) {
            result.add(storage.findUserById(friendId));
        }
        log.info("Получение списка друзей пользователя с id {}.", id);
        return result;
    }

    public List<User> getGeneralFriendList(Long id, Long otherId) throws NotFoundException {
        User firstUser = storage.findUserById(id);
        User secondUser = storage.findUserById(otherId);
        Set<Long> commonFriends = new HashSet<>(firstUser.getFriends());
        commonFriends.retainAll(secondUser.getFriends());

        List<User> result = new ArrayList<>(commonFriends.size());

        for (Long friendId : commonFriends) {
            result.add(storage.findUserById(friendId));
        }
        log.info("Получение списка общих друзей пользователей с id {} и {}.", id, otherId);
        return result;
    }
}