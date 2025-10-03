package com.example.SonicCanopy.service;

import com.example.SonicCanopy.domain.dto.auth.AuthRequest;
import com.example.SonicCanopy.domain.dto.auth.AuthResponse;
import com.example.SonicCanopy.domain.dto.auth.RefreshTokenRequest;
import com.example.SonicCanopy.domain.exception.auth.InvalidCredentialsException;
import com.example.SonicCanopy.domain.exception.auth.InvalidRefreshTokenException;
import com.example.SonicCanopy.domain.exception.auth.RefreshTokenExpiredException;
import com.example.SonicCanopy.security.auth.AuthenticationService;
import com.example.SonicCanopy.security.auth.JwtService;
import com.example.SonicCanopy.service.app.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserService userService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        userService = mock(UserService.class);
        authenticationService = new AuthenticationService(authenticationManager, jwtService, userService);
    }

    // ---------- login tests ----------
    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        AuthRequest request = new AuthRequest("john", "password");
        UserDetails mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("john");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateAccessToken(mockUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(mockUser)).thenReturn("refresh-token");

        AuthResponse result = authenticationService.login(request);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("john", captor.getValue().getPrincipal());
        assertEquals("password", captor.getValue().getCredentials());
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenAuthenticationFails() {
        AuthRequest request = new AuthRequest("john", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(request));
    }

    // ---------- refreshToken tests ----------
    @Test
    void refreshToken_shouldReturnNewAccessToken_whenValid() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        UserDetails mockUser = mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("john");

        when(jwtService.extractUsername("valid-refresh-token")).thenReturn("john");
        when(userService.loadUserByUsername("john")).thenReturn(mockUser);
        when(jwtService.validateToken("valid-refresh-token", mockUser)).thenReturn(true);
        when(jwtService.generateAccessToken(mockUser)).thenReturn("new-access-token");

        AuthResponse result = authenticationService.refreshToken(request);

        assertNotNull(result);
        assertEquals("new-access-token", result.accessToken());
        assertEquals("valid-refresh-token", result.refreshToken());
    }

    @Test
    void refreshToken_shouldThrowInvalidRefreshToken_whenTokenTampered() {
        RefreshTokenRequest request = new RefreshTokenRequest("tampered-token");
        UserDetails mockUser = mock(UserDetails.class);

        when(jwtService.extractUsername("tampered-token")).thenReturn("john");
        when(userService.loadUserByUsername("john")).thenReturn(mockUser);
        when(jwtService.validateToken("tampered-token", mockUser)).thenReturn(false);

        assertThrows(InvalidRefreshTokenException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void refreshToken_shouldThrowRefreshTokenExpired_whenTokenExpired() {
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");

        when(jwtService.extractUsername("expired-token")).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        assertThrows(RefreshTokenExpiredException.class, () -> authenticationService.refreshToken(request));
    }

    @Test
    void refreshToken_shouldThrowInvalidRefreshToken_onOtherExceptions() {
        RefreshTokenRequest request = new RefreshTokenRequest("bad-token");

        when(jwtService.extractUsername("bad-token")).thenThrow(new RuntimeException("other error"));

        assertThrows(InvalidRefreshTokenException.class, () -> authenticationService.refreshToken(request));
    }
}
