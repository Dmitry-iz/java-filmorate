package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.TestConfig;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({TestConfig.class, UserDbStorage.class})
class UserDbStorageTest {
    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void testCreateUser() {
        User createdUser = userStorage.create(testUser);

        assertNotNull(createdUser.getId());
        assertEquals(testUser.getEmail(), createdUser.getEmail());
        assertEquals(testUser.getLogin(), createdUser.getLogin());
    }

    @Test
    void testUpdateUser() {
        User createdUser = userStorage.create(testUser);
        createdUser.setName("Updated Name");

        User updatedUser = userStorage.update(createdUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(createdUser.getId(), updatedUser.getId());
    }

    @Test
    void testGetUserById() {
        User createdUser = userStorage.create(testUser);
        User foundUser = userStorage.getById(createdUser.getId());

        assertEquals(createdUser, foundUser);
    }

    @Test
    void testGetAllUsers() {
        userStorage.create(testUser);
        List<User> users = userStorage.getAll();

        assertThat(users).hasSize(1);
    }

    @Test
    void testAddFriend() {
        User user1 = userStorage.create(testUser);
        User user2 = userStorage.create(User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        userStorage.addFriend(user1.getId(), user2.getId());
        List<User> friends = userStorage.getFriends(user1.getId());

        assertThat(friends).hasSize(1);
        assertEquals(user2.getId(), friends.get(0).getId());
    }

    @Test
    void testRemoveFriend() {
        User user1 = userStorage.create(testUser);
        User user2 = userStorage.create(User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.removeFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user1 = userStorage.create(testUser);
        User user2 = userStorage.create(User.builder()
                .email("user2@example.com")
                .login("user2Login")
                .birthday(LocalDate.of(1995, 5, 5))
                .build());
        User commonFriend = userStorage.create(User.builder()
                .email("common@example.com")
                .login("commonLogin")
                .birthday(LocalDate.of(1996, 6, 6))
                .build());

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertEquals(commonFriend.getId(), commonFriends.get(0).getId());
    }

    @Test
    void testUserNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getById(999));
    }
}
