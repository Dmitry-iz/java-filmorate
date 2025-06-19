package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .email("test@example.com")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldCreateUser() {
        when(userService.create(testUser)).thenReturn(testUser);

        ResponseEntity<User> response = userController.create(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService, times(1)).create(testUser);
    }

    @Test
    void shouldUpdateUser() {
        when(userService.update(testUser)).thenReturn(testUser);

        ResponseEntity<User> response = userController.update(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService, times(1)).update(testUser);
    }

    @Test
    void shouldGetUserById() {
        when(userService.getById(1)).thenReturn(testUser);

        ResponseEntity<User> response = userController.getById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(userService, times(1)).getById(1);
    }

    @Test
    void shouldGetAllUsers() {
        when(userService.getAll()).thenReturn(List.of(testUser));

        ResponseEntity<Collection<User>> response = userController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
        assertTrue(response.getBody().contains(testUser));
        verify(userService, times(1)).getAll();
    }

    @Test
    void shouldAddFriend() {
        doNothing().when(userService).addFriend(1, 2);

        ResponseEntity<Void> response = userController.addFriend(1, 2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).addFriend(1, 2);
    }

    @Test
    void shouldRemoveFriend() {
        doNothing().when(userService).removeFriend(1, 2);

        ResponseEntity<Void> response = userController.removeFriend(1, 2);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).removeFriend(1, 2);
    }

    @Test
    void shouldGetFriends() {
        when(userService.getFriends(1)).thenReturn(List.of(testUser));

        ResponseEntity<Collection<User>> response = userController.getFriends(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertThat(response.getBody()).hasSize(1);
        assertTrue(response.getBody().contains(testUser));
        verify(userService, times(1)).getFriends(1);
    }

    @Test
    void shouldGetCommonFriends() {
        User commonFriend = User.builder()
                .id(2)
                .email("friend@example.com")
                .login("friendLogin")
                .name("Friend")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        when(userService.getCommonFriends(1, 3)).thenReturn(List.of(commonFriend));

        Collection<User> commonFriends = userController.getCommonFriends(1, 3);

        assertThat(commonFriends).hasSize(1);
        assertEquals(commonFriend, commonFriends.iterator().next());
        verify(userService, times(1)).getCommonFriends(1, 3);
    }
}
