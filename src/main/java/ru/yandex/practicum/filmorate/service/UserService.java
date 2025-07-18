package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
        return storage.findUserById(id);
    }

    public User addNewUser(User user) {
        return storage.addNewUser(user);
    }

    public User updateUserInfo(User updatedUser) {
        return storage.updateUserInfo(updatedUser);
    }

    public void deleteUser(Long userId) {
        storage.deleteUser(userId);
    }

    public void addFriend(Long id, Long friendId) {
        storage.addFriend(id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        storage.deleteFriend(id, friendId);
    }

    public List<User> getUserFriendList(Long id) {
        User user = storage.findUserById(id);
        List<User> friends = new ArrayList<>();
        for (Long friendId : user.getFriends()) {
            friends.add(storage.findUserById(friendId));
        }
        return friends;
    }

    public List<User> getGeneralFriendList(Long id, Long otherId) {
        User user1 = storage.findUserById(id);
        User user2 = storage.findUserById(otherId);
        Set<Long> common = new HashSet<>(user1.getFriends());
        common.retainAll(user2.getFriends());

        List<User> result = new ArrayList<>();
        for (Long friendId : common) {
            result.add(storage.findUserById(friendId));
        }
        return result;
    }
}