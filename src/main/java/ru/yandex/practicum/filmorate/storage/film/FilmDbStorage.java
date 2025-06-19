package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {

        // Проверка наличия MPA
        String checkMpaSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer mpaCount = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, film.getMpa().getId());
        if (mpaCount == null || mpaCount == 0) {
            throw new NotFoundException("MPA с id " + film.getMpa().getId() + " не найден");
        }

        // Проверка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String checkGenreSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
            for (Genre genre : film.getGenres()) {
                Integer genreCount = jdbcTemplate.queryForObject(checkGenreSql, Integer.class, genre.getId());
                if (genreCount == null || genreCount == 0) {
                    throw new NotFoundException("Жанр с id " + genre.getId() + " не найден");
                }
            }
        }

        // Создание фильма
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        // Сохранение жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = film.getGenres().stream()
                    .map(genre -> new Object[]{film.getId(), genre.getId()})
                    .toList();
            jdbcTemplate.batchUpdate(genreSql, batchArgs);
        }

        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        // Проверка существования MPA
        String checkMpaSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer mpaCount = jdbcTemplate.queryForObject(checkMpaSql, Integer.class, film.getMpa().getId());
        if (mpaCount == null || mpaCount == 0) {
            throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
        }

        // Обновление основных данных фильма
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        // Обновление жанров
        updateFilmGenres(film);

        return getById(film.getId());
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "ORDER BY f.id";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm);
        return films;
    }

    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description, " +
                "COUNT(fl.user_id) AS likes_count FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToFilm, count);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = Mpa.builder()
                .id(rs.getInt("mpa_id"))
                .name(rs.getString("mpa_name"))
                .description(rs.getString("mpa_description"))
                .build();

        Film film = Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();

        film.getGenres().addAll(getFilmGenres(film.getId()));
        film.getLikes().addAll(getFilmLikes(film.getId()));
        return film;
    }

    private Set<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? ORDER BY g.id"; // Сортируем по id жанра
        return new LinkedHashSet<>(jdbcTemplate.query(sql, this::mapRowToGenre, filmId));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }

    private Set<Integer> getFilmLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }

    private void updateFilmGenres(Film film) {
        // Удаляем старые жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        // Добавляем новые, если они есть

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Удаляем дубликаты, сохраняя порядок первого вхождения
            Set<Integer> addedGenres = new HashSet<>();
            List<Genre> uniqueGenres = new ArrayList<>();

            for (Genre genre : film.getGenres()) {
                if (!addedGenres.contains(genre.getId())) {
                    uniqueGenres.add(genre);
                    addedGenres.add(genre.getId());
                }
            }

            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            List<Object[]> batchArgs = uniqueGenres.stream()
                    .map(genre -> new Object[]{film.getId(), genre.getId()})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }
}


