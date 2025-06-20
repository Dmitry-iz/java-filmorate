package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.TestConfig;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestConfig.class, FilmDbStorage.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        // Очищаем все таблицы перед тестом
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");

        Set<Genre> genres = new HashSet<>();
        genres.add(Genre.builder().id(1).name("Комедия").build());
        genres.add(Genre.builder().id(2).name("Драма").build());

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).name("G").description("General Audiences").build())
                .genres(genres)
                .build();
    }

    @Test
    void shouldCreateAndGetFilm() {
        Film createdFilm = filmStorage.create(testFilm);
        Film retrievedFilm = filmStorage.getById(createdFilm.getId());

        assertThat(retrievedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("likes")
                .isEqualTo(createdFilm);
    }
}