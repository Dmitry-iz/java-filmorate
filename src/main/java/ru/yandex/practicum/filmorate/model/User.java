package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"id"})
public class User {
    @NotNull(groups = OnUpdate.class) // id обязателен при обновлении
    @PositiveOrZero
    private Integer id;
    private final Set<Integer> friends = new HashSet<>();

    public Set<Integer> getFriends() {
        return friends;
    }

    @Email(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "Некорректный формат email")
    @NotBlank(message = "Электронная почта не может быть пустой")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}