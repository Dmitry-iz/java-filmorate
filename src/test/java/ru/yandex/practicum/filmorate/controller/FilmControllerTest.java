package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmControllerTest {
    @Mock
    private FilmService filmService;

    @InjectMocks
    private FilmController filmController;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = Film.builder()
                .id(1)
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa(1, "G", "General Audiences"))
                .build();
    }

    @Test
    void shouldCreateFilm() {
        when(filmService.createFilm(testFilm)).thenReturn(testFilm);

        ResponseEntity<Film> response = filmController.create(testFilm);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testFilm, response.getBody());
        verify(filmService, times(1)).createFilm(testFilm);
    }

    @Test
    void shouldUpdateFilm() {
        when(filmService.updateFilm(testFilm)).thenReturn(testFilm);

        ResponseEntity<Film> response = filmController.update(testFilm);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testFilm, response.getBody());
        verify(filmService, times(1)).updateFilm(testFilm);
    }

    @Test
    void shouldGetFilmById() {
        when(filmService.getFilmById(1)).thenReturn(testFilm);

        Film result = filmController.getById(1);

        assertEquals(testFilm, result);
        verify(filmService, times(1)).getFilmById(1);
    }

    @Test
    void shouldGetAllFilms() {
        when(filmService.getAllFilms()).thenReturn(List.of(testFilm));

        List<Film> films = filmController.getAll();

        assertThat(films).hasSize(1);
        assertEquals(testFilm, films.get(0));
        verify(filmService, times(1)).getAllFilms();
    }

    @Test
    void shouldAddLike() {
        doNothing().when(filmService).addLike(1, 1);

        filmController.addLike(1, 1);

        verify(filmService, times(1)).addLike(1, 1);
    }

    @Test
    void shouldRemoveLike() {
        doNothing().when(filmService).removeLike(1, 1);

        filmController.removeLike(1, 1);

        verify(filmService, times(1)).removeLike(1, 1);
    }

    @Test
    void shouldGetPopularFilms() {
        when(filmService.getPopularFilms(10)).thenReturn(List.of(testFilm));

        List<Film> popularFilms = filmController.getPopular(10);

        assertThat(popularFilms).hasSize(1);
        assertEquals(testFilm, popularFilms.get(0));
        verify(filmService, times(1)).getPopularFilms(10);
    }
}