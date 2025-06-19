package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(int id) {
        try {
            return Optional.ofNullable(genreStorage.getGenreById(id))
                    .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
        } catch (Exception e) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
    }
}
