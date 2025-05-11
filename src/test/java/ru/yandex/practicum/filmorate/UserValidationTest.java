package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUser = User.builder()
                .email("test@mail.ru")
                .login("validLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void shouldPassValidationWithCorrectData() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        validUser.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        validUser.setLogin("invalid login");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenBirthdayIsFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailHasInvalidChars() {
        validUser.setEmail("кириллица@домен.рф");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertFalse(violations.isEmpty());
    }
}
