package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaService mpaService;
    private final GenreService genreService;

    @Autowired
    public FilmService(FilmStorage filmStorage, MpaService mpaService, GenreService genreService) {
        this.filmStorage = filmStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
    }

    public Film createFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительной");
        }
        if (film.getMpa() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }

        Mpa mpa = mpaService.getMpaById(film.getMpa().getId());
        if (mpa == null) {
            throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
        }

        Set<Genre> processedGenres = new LinkedHashSet<>();
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Genre existingGenre = genreService.getGenreById(genre.getId());
                processedGenres.add(existingGenre);
            }
        }
        film.setGenres(processedGenres);

        return filmStorage.create(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        if (film.getMpa() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }

        Set<Genre> processedGenres = new LinkedHashSet<>();
        if (film.getGenres() != null) {
            Set<Integer> genreIds = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (!genreIds.contains(genre.getId())) {
                    Genre existingGenre = genreService.getGenreById(genre.getId());
                    processedGenres.add(existingGenre);
                    genreIds.add(genre.getId());
                }
            }
        }
        film.setGenres(processedGenres);

        return filmStorage.update(film);
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }
}


