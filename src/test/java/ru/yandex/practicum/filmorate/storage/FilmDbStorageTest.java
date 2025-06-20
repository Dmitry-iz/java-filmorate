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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({TestConfig.class, FilmDbStorage.class})
class FilmDbStorageTest {
    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        Set<Genre> genres = new HashSet<>();
        genres.add(Genre.builder().id(1).build());

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(Mpa.builder().id(1).build())
                .genres(genres)
                .build();
    }

    @Test
    void testCreateFilm() {
        Film createdFilm = filmStorage.create(testFilm);

        assertNotNull(createdFilm.getId());
        assertEquals(testFilm.getName(), createdFilm.getName());
        assertThat(createdFilm.getGenres()).hasSize(1);
    }

    @Test
    void testUpdateFilm() {
        Film createdFilm = filmStorage.create(testFilm);
        createdFilm.setName("Updated Film");

        Film updatedFilm = filmStorage.update(createdFilm);

        assertEquals("Updated Film", updatedFilm.getName());
        assertEquals(createdFilm.getId(), updatedFilm.getId());
    }

    @Test
    void testGetFilmById() {
        Film createdFilm = filmStorage.create(testFilm);
        Film foundFilm = filmStorage.getById(createdFilm.getId());

        assertEquals(createdFilm, foundFilm);
    }

    @Test
    void testGetAllFilms() {
        filmStorage.create(testFilm);
        List<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(1);
    }

    @Test
    void testAddLike() {
        Film film = filmStorage.create(testFilm);
        User user = createTestUser();

        filmStorage.addLike(film.getId(), user.getId());
        List<Film> popularFilms = filmStorage.getPopularFilms(1);

        assertThat(popularFilms).hasSize(1);
        assertEquals(film.getId(), popularFilms.get(0).getId());
    }

    @Test
    void testRemoveLike() {
        Film film = filmStorage.create(testFilm);
        User user = createTestUser();

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(1);
        assertThat(popularFilms).hasSize(1);
    }

    @Test
    void testGetPopularFilms() {
        Film film1 = filmStorage.create(testFilm);
        Film film2 = filmStorage.create(Film.builder()
                .name("Another Film")
                .description("Another Description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(90)
                .mpa(Mpa.builder().id(2).build())
                .build());

        User user = createTestUser();
        filmStorage.addLike(film1.getId(), user.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(2);
        assertThat(popularFilms).hasSize(2);
        assertEquals(film1.getId(), popularFilms.get(0).getId());
    }

    private User createTestUser() {
        return new UserDbStorage(jdbcTemplate).create(User.builder()
                .email("user@example.com")
                .login("userLogin")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());
    }
}