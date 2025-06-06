package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int userIdCounter = 1;

    @Override
    public User create(User user) {
        user.setId(userIdCounter++);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(int id) {
        return users.get(id);
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }
}
