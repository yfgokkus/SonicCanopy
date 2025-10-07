package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.config.FirebaseTestConfig;
import com.example.SonicCanopy.domain.entity.Role;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.domain.dto.auth.AuthRequest;
import com.example.SonicCanopy.domain.dto.auth.RefreshTokenRequest;
import com.example.SonicCanopy.repository.UserRepository;
import com.example.SonicCanopy.security.auth.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
@Transactional
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private final String testUsername = "auth_integration_user";
    private final String testPassword = "P@ssw0rd!";

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("Auth Integration")
                .username(testUsername)
                .password(passwordEncoder.encode(testPassword))
                .authorities(Set.of(Role.ROLE_USER))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(user);
    }

    @Test
    void login_shouldReturnAccessAndRefreshTokens_whenCredentialsValid() throws Exception {
        AuthRequest request = new AuthRequest(testUsername, testPassword);

        String responseJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer")) // added token type check
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(responseJson).at("/data/accessToken").asText();
        String refreshToken = objectMapper.readTree(responseJson).at("/data/refreshToken").asText();

        assertThat(jwtService.extractUsername(accessToken)).isEqualTo(testUsername);
        assertThat(jwtService.extractUsername(refreshToken)).isEqualTo(testUsername);
        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        AuthRequest request = new AuthRequest(testUsername, "wrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturnNewAccessToken_whenRefreshTokenValid() throws Exception {
        AuthRequest loginRequest = new AuthRequest(testUsername, testPassword);

        String loginJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginJson).at("/data/refreshToken").asText();

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        String refreshJson = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken)) // same refresh token
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String newAccessToken = objectMapper.readTree(refreshJson).at("/data/accessToken").asText();

        assertThat(newAccessToken).isNotBlank();
        assertThat(jwtService.extractUsername(newAccessToken)).isEqualTo(testUsername);
    }
}
