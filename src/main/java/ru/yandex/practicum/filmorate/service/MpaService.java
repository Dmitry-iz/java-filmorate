package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.Optional;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }

    public Mpa getMpaById(int id) {
        try {
            return Optional.ofNullable(mpaStorage.getMpaById(id))
                    .orElseThrow(() -> new NotFoundException("MPA с id=" + id + " не найден"));
        } catch (Exception e) {
            throw new NotFoundException("MPA с id=" + id + " не найден");
        }
    }
}