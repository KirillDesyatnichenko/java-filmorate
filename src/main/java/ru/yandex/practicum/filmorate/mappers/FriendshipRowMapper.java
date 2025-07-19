package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Friendship;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipRowMapper implements RowMapper<Friendship> {

    @Override
    public Friendship mapRow(ResultSet rs, int rowNum) throws SQLException {
        Friendship friendship = new Friendship();
        friendship.setId(rs.getLong("friendship_id"));
        friendship.setInitiatorUserId(rs.getLong("initiator_user_id"));
        friendship.setFriendUserId(rs.getLong("friend_user_id"));
        return friendship;
    }
}