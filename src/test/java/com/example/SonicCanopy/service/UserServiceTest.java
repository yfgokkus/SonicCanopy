package com.example.SonicCanopy.service;

import com.example.SonicCanopy.domain.dto.user.CreateUserRequest;
import com.example.SonicCanopy.domain.entity.Role;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.domain.exception.user.UsernameAlreadyExistsException;
import com.example.SonicCanopy.repository.UserRepository;
import com.example.SonicCanopy.service.app.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    // ---------- createUser tests ----------
    @Test
    void createUser_shouldSaveUser_whenUsernameDoesNotExist() {
        CreateUserRequest request = new CreateUserRequest(
                "John Doe", "john123", "password"
        );

        when(userRepository.existsByUsername("john123")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .fullName("John Doe")
                .username("john123")
                .password("encodedPassword")
                .authorities(Set.of(Role.ROLE_USER))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("john123", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenUsernameExists() {
        CreateUserRequest request = new CreateUserRequest(
                "John Doe", "john123", "password"
        );

        when(userRepository.existsByUsername("john123")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.createUser(request));

        verify(userRepository, never()).save(any(User.class));
    }

    // ---------- loadUserByUsername tests ----------
    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        User user = User.builder()
                .username("john123")
                .password("encodedPassword")
                .build();

        when(userRepository.findByUsername("john123")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("john123");

        assertNotNull(userDetails);
        assertEquals("john123", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername("john123")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("john123")
        );

        assertEquals("User not found: john123", exception.getMessage());
    }

    // ---------- getByUsername tests ----------
    @Test
    void getByUsername_shouldReturnUser_whenUserExists() {
        User user = User.builder().username("john123").build();
        when(userRepository.findByUsername("john123")).thenReturn(Optional.of(user));

        User result = userService.getByUsername("john123");

        assertNotNull(result);
        assertEquals("john123", result.getUsername());
    }

    @Test
    void getByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername("john123")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.getByUsername("john123")
        );

        assertEquals("User not found", exception.getMessage());
    }
}

