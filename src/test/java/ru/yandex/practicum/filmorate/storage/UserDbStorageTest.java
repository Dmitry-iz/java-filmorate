package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestConfig.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate; // Добавляем внедрение JdbcTemplate

    private User testUser;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");

        testUser = User.builder()
                .email("test@example.com")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldCreateAndGetUser() {
        User createdUser = userStorage.create(testUser);
        User retrievedUser = userStorage.getById(createdUser.getId());

        assertThat(retrievedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(createdUser);
    }

    @Test
    void shouldUpdateUser() {
        User createdUser = userStorage.create(testUser);
        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@example.com");

        User updatedUser = userStorage.update(createdUser);
        User retrievedUser = userStorage.getById(createdUser.getId());

        assertEquals("Updated Name", retrievedUser.getName());
        assertEquals("updated@example.com", retrievedUser.getEmail());
    }

    @Test
    void shouldGetAllUsers() {
        // Создаем тестовых пользователей
        userStorage.create(testUser);
        User anotherUser = User.builder()
                .email("another@example.com")
                .login("anotherLogin")
                .name("Another Name")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();
        userStorage.create(anotherUser);

        List<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = userStorage.create(testUser);
        User user2 = User.builder()
                .email("friend@example.com")
                .login("friendLogin")
                .name("Friend Name")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();
        User friend = userStorage.create(user2);

        userStorage.addFriend(user1.getId(), friend.getId());
        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1);
        assertEquals(friend.getId(), friends.get(0).getId());

        userStorage.removeFriend(user1.getId(), friend.getId());
        List<User> friendsAfterRemoval = userStorage.getFriends(user1.getId());
        assertThat(friendsAfterRemoval).isEmpty();
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userStorage.create(testUser);
        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2Login")
                .name("User 2")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();
        user2 = userStorage.create(user2);

        User commonFriend = User.builder()
                .email("common@example.com")
                .login("commonLogin")
                .name("Common Friend")
                .birthday(LocalDate.of(1992, 1, 1))
                .build();
        commonFriend = userStorage.create(commonFriend);

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertEquals(commonFriend.getId(), commonFriends.get(0).getId());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userStorage.getById(999));
    }
}
