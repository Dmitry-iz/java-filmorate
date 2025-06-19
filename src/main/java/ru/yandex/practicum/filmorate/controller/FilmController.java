package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        try {
            Film createdFilm = filmService.createFilm(film);
            return ResponseEntity.ok(createdFilm);
        } catch (NotFoundException e) {
            log.error("Не найдено: {}", e.getMessage());
            throw e;
        } catch (ValidationException e) {
            log.error("Ошибка валидации: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Внутренняя ошибка: {}", e.getMessage());
            throw new ValidationException("Внутренняя ошибка сервера");
        }
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        log.info("PUT /films - обновление фильма: {}", film);
        try {
            Film updatedFilm = filmService.updateFilm(film);
            return ResponseEntity.ok(updatedFilm);
        } catch (NotFoundException e) {
            log.error("Фильм не найден: {}", film.getId(), e);
            throw e;
        } catch (ValidationException e) {
            log.error("Ошибка валидации: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @GetMapping
    public List<Film> getAll() {
        return filmService.getAllFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}