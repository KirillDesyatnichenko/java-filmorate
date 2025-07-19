package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {

    Collection<User> getUserList();

    User addNewUser(User user);

    User updateUserInfo(User updatedUser);

    void deleteUser(Long userId);

    Optional<User> findUserById(Long userId);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<User> findUsersByIds(Collection<Long> ids);

    boolean isUserNotExists(Long id);
}