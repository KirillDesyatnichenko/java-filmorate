package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;


import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, ru.yandex.practicum.filmorate.mappers.UserRowMapper.class})
class FilmoRateApplicationTests {
	private final UserDbStorage userStorage;

	@Test
	public void testFindUserById() {
		User user = new User();
		user.setName("Шиз");
		user.setEmail("Lol@example.com");
		user.setLogin("Шиз123");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		userStorage.addNewUser(user);

		User foundUser = userStorage.findUserById(1L);

		assertThat(foundUser)
				.isNotNull()
				.extracting(User::getName, User::getEmail, User::getLogin, User::getBirthday)
				.containsExactly("Шиз", "Lol@example.com", "Шиз123", LocalDate.of(1990, 1, 1));
	}
}
