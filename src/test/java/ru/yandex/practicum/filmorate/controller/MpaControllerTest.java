package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MpaControllerTest {
    @Mock
    private MpaService mpaService;

    @InjectMocks
    private MpaController mpaController;

    @Test
    void shouldGetAllMpa() {
        Mpa mpa = new Mpa(1, "G", "General Audiences");
        when(mpaService.getAllMpa()).thenReturn(List.of(mpa));

        ResponseEntity<List<Mpa>> response = mpaController.getAllMpa();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
        assertEquals(mpa, response.getBody().get(0));
        verify(mpaService, times(1)).getAllMpa();
    }

    @Test
    void shouldGetMpaById() {
        Mpa mpa = new Mpa(1, "G", "General Audiences");
        when(mpaService.getMpaById(1)).thenReturn(mpa);

        ResponseEntity<Mpa> response = mpaController.getMpaById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mpa, response.getBody());
        verify(mpaService, times(1)).getMpaById(1);
    }
}
