package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id", "email"})
public class User {
    private Long id;
    @Email(message = "Некорректный адрес электронной почты.")
    private String email;
    @NotBlank(message = "Логин должен быть заполнен и не содержать пробелов!")
    private String login;
    private String name;
    private LocalDate birthday;


    public void updateFrom(User updatedUser) {
        if (updatedUser.getLogin() != null && !updatedUser.getLogin().isBlank()) {
            this.login = updatedUser.getLogin();
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
            this.email = updatedUser.getEmail();
        }
        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
            this.name = updatedUser.getName();
        }
        if (updatedUser.getBirthday() != null) {
            this.birthday = updatedUser.getBirthday();
        }
    }
}