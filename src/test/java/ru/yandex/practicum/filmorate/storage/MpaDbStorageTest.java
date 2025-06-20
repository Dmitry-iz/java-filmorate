package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.TestConfig;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestConfig.class, MpaDbStorage.class})
class MpaDbStorageTest {
    @Autowired
    private MpaDbStorage mpaStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы, которые могут влиять на тесты
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
    }

    @Test
    void shouldGetMpaById() {
        Mpa mpa = mpaStorage.getMpaById(1);
        assertNotNull(mpa);
        assertEquals("G", mpa.getName());
        assertEquals("Нет возрастных ограничений", mpa.getDescription());
    }

    @Test
    void shouldGetAllMpa() {
        List<Mpa> mpaRatings = mpaStorage.getAllMpa();
        assertThat(mpaRatings).hasSize(5);
        assertThat(mpaRatings).extracting(Mpa::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }
}
