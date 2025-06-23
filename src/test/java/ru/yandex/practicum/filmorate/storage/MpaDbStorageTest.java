package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.TestConfig;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({TestConfig.class, MpaDbStorage.class})
class MpaDbStorageTest {
    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void testGetMpaById() {
        Mpa mpa = mpaStorage.getMpaById(1);

        assertEquals(1, mpa.getId());
        assertEquals("G", mpa.getName());
        assertEquals("Нет возрастных ограничений", mpa.getDescription());
    }

    @Test
    void testGetAllMpa() {
        List<Mpa> mpaRatings = mpaStorage.getAllMpa();

        assertThat(mpaRatings).hasSize(5);
        assertThat(mpaRatings).extracting(Mpa::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }
}
