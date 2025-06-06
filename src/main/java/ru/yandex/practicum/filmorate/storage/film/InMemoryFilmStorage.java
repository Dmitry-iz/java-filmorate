package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmIdCounter = 1;

    @Override
    public Film create(Film film) {
        film.setId(filmIdCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getById(int id) {
        return films.get(id);
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public void delete(int id) {
        films.remove(id);
    }
}
