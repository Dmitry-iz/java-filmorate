package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.*;

@Data
@Builder
public class Film {
    @NotNull(groups = OnUpdate.class)
    private Integer id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    @NotNull(message = "Рейтинг MPA обязателен")
    private Mpa mpa;

    @Builder.Default
    private Set<Genre> genres = new LinkedHashSet<>();

    @Builder.Default
    private Set<Integer> likes = new HashSet<>();

    @AssertTrue(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}