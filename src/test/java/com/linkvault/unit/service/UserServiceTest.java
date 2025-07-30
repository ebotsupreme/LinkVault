package com.linkvault.unit.service;

import com.linkvault.exception.RegistrationFailedException;
import com.linkvault.exception.UsernameAlreadyExistsException;
import com.linkvault.model.User;
import com.linkvault.repository.UserRepository;
import com.linkvault.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userServiceImpl = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUser() {
        // Arrange
        String username = "newUser";
        String rawPassword = "validPassword123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(
            invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            }
        );

        // Act
        userServiceImpl.registerUser(username, rawPassword);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(username, savedUser.getUsername());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }

    @Test
    void shouldThrowUsernameAlreadyExistsException_WhenUsernameAlreadyExists() {
        // Arrange
        String username = "existingUser";
        String rawPassword = "validPassword123";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () ->
            userServiceImpl.registerUser(username, rawPassword));
    }

    @Test
    void shouldThrowRegistrationFailedException_WhenSavingUserFails() {
        // Arrange
        String username = "existingUser";
        String rawPassword = "validPassword123";
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.save(any(User.class)))
            .thenThrow(new RuntimeException("Database write failed"));

        // Act & Assert
        RegistrationFailedException exception = assertThrows(
            RegistrationFailedException.class,
            () -> userServiceImpl.registerUser(username, rawPassword)
        );

        assertEquals("Unexpected error during registration for user: " + username, exception.getMessage());
        assertEquals("Database write failed", exception.getCause().getMessage());
    }
}
