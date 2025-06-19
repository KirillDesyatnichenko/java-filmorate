package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id", "email"})
public class User {
    private Long id;
    @NotNull(message = "Адрес электронной почты не заполнен!")
    @Email(message = "Некорректный адрес электронной почты!")
    private String email;
    @NotBlank(message = "Логин должен быть заполнен!")
    private String login;
    private String name;
    @NotNull(message = "Дата рождения не заполнена!")
    private LocalDate birthday;

    private Set<Long> friends = new HashSet<>();

    public void updateFrom(User updatedUser) {
        if (updatedUser.getLogin() != null && !updatedUser.getLogin().isBlank()) {
            this.login = updatedUser.getLogin();
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
            this.email = updatedUser.getEmail();
        }
        setDefaultNameIfEmpty(this.login);
        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
            this.name = updatedUser.getName();
        }
        if (updatedUser.getBirthday() != null) {
            this.birthday = updatedUser.getBirthday();
        }
    }

    public void setDefaultNameIfEmpty(String login) {
        if (this.name == null || this.name.isBlank()) {
            this.name = login;
        }
    }
}