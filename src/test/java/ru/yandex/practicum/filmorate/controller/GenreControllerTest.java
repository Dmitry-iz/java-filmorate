package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreControllerTest {
    @Mock
    private GenreService genreService;

    @InjectMocks
    private GenreController genreController;

    @Test
    void shouldGetAllGenres() {
        Genre genre = new Genre(1, "Комедия");
        when(genreService.getAllGenres()).thenReturn(List.of(genre));

        ResponseEntity<List<Genre>> response = genreController.getAllGenres();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
        assertEquals(genre, response.getBody().get(0));
        verify(genreService, times(1)).getAllGenres();
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = new Genre(1, "Комедия");
        when(genreService.getGenreById(1)).thenReturn(genre);

        ResponseEntity<Genre> response = genreController.getGenreById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genre, response.getBody());
        verify(genreService, times(1)).getGenreById(1);
    }
}
