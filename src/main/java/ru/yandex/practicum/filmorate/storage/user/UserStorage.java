package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User getById(int id);

    List<User> getAll();

    void delete(int id);

    void addFriend(int userId, int friendId);

    void confirmFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    List<User> getFriends(int userId);

    List<User> getFriendRequests(int userId);

    List<User> getCommonFriends(int userId, int otherId);

    default boolean userExists(int userId) {
        try {
            getById(userId);
            return true;
        } catch (NotFoundException e) {
            return false;
       }
    }
}