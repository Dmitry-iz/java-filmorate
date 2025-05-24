package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.Collection;
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

    @GetMapping
    public ResponseEntity<Collection<Film>> getAll() {
        return ResponseEntity.ok(filmService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable int id) {
        return ResponseEntity.ok(filmService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        Film createdFilm = filmService.create(film);
        log.info("Добавлен фильм: {}", createdFilm);
        return ResponseEntity.ok(createdFilm);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Validated(OnUpdate.class) @RequestBody Film film) {
        Film existingFilm = filmService.getById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм не найден");
        }
        Film updatedFilm = filmService.update(film);
        log.info("Обновлён фильм: {}", updatedFilm);
        return ResponseEntity.ok(updatedFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopular(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(filmService.getPopularFilms(count));
    }
}