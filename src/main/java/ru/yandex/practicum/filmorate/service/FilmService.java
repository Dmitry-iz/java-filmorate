package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getById(filmId);
        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}