package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.user.CreateUserRequest;
import com.example.SonicCanopy.domain.entity.Role;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String testUsername = "integration_user";
    private final String testPassword = "P@ssw0rd!";
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a user in the DB
        testUser = User.builder()
                .fullName("Integration Test User")
                .username(testUsername)
                .password(passwordEncoder.encode(testPassword))
                .authorities(Set.of(Role.ROLE_USER))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();

        userRepository.save(testUser);
    }

    @Test
    void register_shouldCreateNewUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("New User", "newuser", "newPassword123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User successfully created"))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.fullName").value("New User"))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void register_shouldReturnConflict_whenUsernameExists() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Test User", testUsername, "password");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUser_shouldReturnUserDto_whenUsernameExists() throws Exception {
        mockMvc.perform(get("/users/{username}", testUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(testUsername))
                .andExpect(jsonPath("$.data.fullName").value("Integration Test User"))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void getUser_shouldReturnNotFound_whenUsernameDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/{username}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "integration_user")
    void getMe_shouldReturnCurrentUser()
            throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(testUsername))
                .andExpect(jsonPath("$.data.fullName").value("Integration Test User"));
    }

    @Test
    void getAdminString_shouldReturnAccessGranted() throws Exception {
        mockMvc.perform(get("/users/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Access granted"));
    }
}
