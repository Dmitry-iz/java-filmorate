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
        // Проверка даты релиза
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        // Проверка продолжительности
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительной");
        }

        // Проверка MPA
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }

        try {
            // Проверка существования MPA
            Mpa mpa = mpaService.getMpaById(film.getMpa().getId());
            if (mpa == null) {
                throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
            }

            if (film.getGenres() != null) {
                for (Genre genre : film.getGenres()) {

                    try {
                        genreService.getGenreById(genre.getId());
                    } catch (NotFoundException e) {
                        throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                    }
                }
            }

            return filmStorage.create(film);
        } catch (NotFoundException e) {
            throw e; // Пробрасываем NotFoundException
        } catch (Exception e) {
            throw new ValidationException("Ошибка при создании фильма: " + e.getMessage());
        }
    }

    public Film updateFilm(Film film) {
        // Проверка существования фильма
        Film existingFilm = filmStorage.getById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        // Проверка MPA
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA рейтинг обязателен");
        }

        Mpa mpa = mpaService.getMpaById(film.getMpa().getId());
        if (mpa == null) {
            throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
        }

        // Проверка жанров
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                Genre existingGenre = genreService.getGenreById(genre.getId());
                if (existingGenre == null) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }

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


