package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    private static final String GET_USER_SQL = """
        SELECT *
        FROM users
        WHERE user_id = ?
        """;

    private static final String GET_ALL_USERS_SQL = """
        SELECT *
        FROM users
        """;

    private static final String UPDATE_USER_SQL = """
        UPDATE users
        SET email = ?, login = ?, name = ?, birthday = ?
        WHERE user_id = ?
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

    @Override
    public Optional<User> findUserById(Long id) {
        List<User> result = jdbcTemplate.query(GET_USER_SQL, userRowMapper, id);
        if (result.isEmpty()) return Optional.empty();

        User user = result.getFirst();
        user.setFriends(loadFriends(id));
        user.setLikedFilms(loadLikes(id));
        return Optional.of(user);
    }

    @Override
    public Collection<User> getUserList() {
        return jdbcTemplate.query(GET_ALL_USERS_SQL, userRowMapper);
    }

    @Override
    public User addNewUser(User user) {
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

        return user;
    }

    @Override
    public User updateUserInfo(User user) {
        jdbcTemplate.update(
                UPDATE_USER_SQL,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return findUserById(user.getId()).orElse(null);
    }

    @Override
    public void deleteUser(Long userId) {
        jdbcTemplate.update(DELETE_USER_SQL, userId);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        Integer reverseExists = jdbcTemplate.queryForObject(
                CHECK_REVERSE_FRIENDSHIP_SQL,
                Integer.class,
                friendId, userId
        );
        if (reverseExists > 0) {
            jdbcTemplate.update(
                    UPDATE_CONFIRMED_FRIENDSHIP_SQL,
                    userId, friendId, friendId, userId
            );
        } else {
            jdbcTemplate.update(INSERT_FRIEND_REQUEST_SQL, userId, friendId);
        }
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.update(DELETE_FRIEND_SQL, userId, friendId);
        jdbcTemplate.update(UPDATE_REVERSE_UNCONFIRMED_SQL, friendId, userId);
    }

    @Override
    public boolean isUserNotExists(Long id) {
        Integer count = jdbcTemplate.queryForObject(CHECK_USER_EXISTS_SQL, Integer.class, id);
        return count <= 0;
    }

    private Set<Long> loadFriends(Long userId) {
        return new HashSet<>(jdbcTemplate.queryForList(LOAD_FRIENDS_SQL, Long.class, userId));
    }

    private Set<Long> loadLikes(Long userId) {
        return new HashSet<>(jdbcTemplate.queryForList(LOAD_LIKES_SQL, Long.class, userId));
    }

    @Override
    public List<User> findUsersByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM users WHERE user_id IN (" + inSql + ")";
        return jdbcTemplate.query(sql, userRowMapper, ids.toArray());
    }
}