package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {
    private final MpaDbStorage mpaStorage;

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
