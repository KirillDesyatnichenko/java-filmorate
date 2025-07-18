package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper;

    private static final String ADD_USER_SQL = """
            INSERT INTO users (login, email, name, birthday)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_USER_SQL = """
            UPDATE users
            SET email = ?, login = ?, name = ?, birthday = ?
            WHERE user_id = ?
            """;

    private static final String GET_USER_SQL = """
            SELECT * FROM users
            WHERE user_id = ?
            """;

    private static final String GET_ALL_USERS_SQL = """
            SELECT * FROM users
            """;

    private static final String DELETE_USER_SQL = """
            DELETE FROM users
            WHERE user_id = ?
            """;

    private static final String LOAD_FRIENDS_SQL = """
            SELECT friend_user_id
            FROM friendship
            WHERE initiator_user_id = ?
            """;

    private static final String LOAD_LIKES_SQL = """
            SELECT film_id
            FROM likes
            WHERE user_id = ?
            """;

    private static final String CHECK_USER_EXISTS_SQL = """
            SELECT COUNT(*)
            FROM users
            WHERE user_id = ?
            """;

    private static final String CHECK_REVERSE_FRIENDSHIP_SQL = """
            SELECT COUNT(*)
            FROM friendship
            WHERE initiator_user_id = ? AND friend_user_id = ?
            """;

    private static final String UPDATE_CONFIRMED_FRIENDSHIP_SQL = """
            UPDATE friendship
            SET confirmed = TRUE
            WHERE (initiator_user_id = ? AND friend_user_id = ?)
               OR (initiator_user_id = ? AND friend_user_id = ?)
            """;

    private static final String INSERT_FRIEND_REQUEST_SQL = """
            INSERT INTO friendship (initiator_user_id, friend_user_id, confirmed)
            VALUES (?, ?, FALSE)
            """;

    private static final String DELETE_FRIEND_SQL = """
            DELETE FROM friendship
            WHERE initiator_user_id = ? AND friend_user_id = ?
            """;

    private static final String UPDATE_REVERSE_UNCONFIRMED_SQL = """
            UPDATE friendship
            SET confirmed = FALSE
            WHERE initiator_user_id = ? AND friend_user_id = ?
            """;

    public UserDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public User findUserById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(GET_USER_SQL, userRowMapper, id);
            if (user != null) {
                user.setFriends(loadFriends(id));
                user.setLikedFilms(loadLikes(id));
            }
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
    }

    @Override
    public Collection<User> getUserList() {
        return jdbcTemplate.query(GET_ALL_USERS_SQL, userRowMapper);
    }

    @Override
    public User addNewUser(User user) {
        validateUser(user);
        user.setDefaultNameIfEmpty(user.getLogin());

        SimpleJdbcInsert insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("login", user.getLogin());
        parameters.put("email", user.getEmail());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());

        Number generatedId = insertUser.executeAndReturnKey(parameters);
        user.setId(generatedId.longValue());

        log.info("Пользователь добавлен: {}", user);
        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Id пользователя должен быть указан при обновлении.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (!isUserExists(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
        }

        jdbcTemplate.update(
                UPDATE_USER_SQL,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        return findUserById(user.getId());
    }

    private boolean isUserExists(Long id) {
        Integer count = jdbcTemplate.queryForObject(CHECK_USER_EXISTS_SQL, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void deleteUser(Long userId) {
        if (findUserSafe(userId) == null) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не найден.");
        }
        jdbcTemplate.update(DELETE_USER_SQL, userId);
        log.info("Пользователь удалён: {}", userId);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }

        findUserById(userId);
        findUserById(friendId);

        Integer reverseExists = jdbcTemplate.queryForObject(CHECK_REVERSE_FRIENDSHIP_SQL, Integer.class, friendId, userId);

        if (reverseExists > 0) {
            jdbcTemplate.update(
                    UPDATE_CONFIRMED_FRIENDSHIP_SQL,
                    userId, friendId, friendId, userId
            );
        } else {
            jdbcTemplate.update(INSERT_FRIEND_REQUEST_SQL, userId, friendId);
        }

        log.info("Пользователь {} отправил заявку в друзья к {}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей.");
        }

        findUserById(userId);
        findUserById(friendId);

        jdbcTemplate.update(DELETE_FRIEND_SQL, userId, friendId);
        jdbcTemplate.update(UPDATE_REVERSE_UNCONFIRMED_SQL, friendId, userId);

        log.info("Пользователь {} удалил друга {}", userId, friendId);
    }

    private Set<Long> loadFriends(Long userId) {
        return new HashSet<>(jdbcTemplate.queryForList(LOAD_FRIENDS_SQL, Long.class, userId));
    }

    private Set<Long> loadLikes(Long userId) {
        return new HashSet<>(jdbcTemplate.queryForList(LOAD_LIKES_SQL, Long.class, userId));
    }

    private User findUserSafe(Long id) {
        try {
            return jdbcTemplate.queryForObject(GET_USER_SQL, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
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
