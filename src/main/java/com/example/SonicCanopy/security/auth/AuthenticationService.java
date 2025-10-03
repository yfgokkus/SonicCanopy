package com.example.SonicCanopy.security.auth;

import com.example.SonicCanopy.domain.dto.auth.AuthRequest;
import com.example.SonicCanopy.domain.dto.auth.AuthResponse;
import com.example.SonicCanopy.domain.dto.auth.RefreshTokenRequest;
import com.example.SonicCanopy.domain.exception.auth.InvalidCredentialsException;
import com.example.SonicCanopy.domain.exception.auth.InvalidRefreshTokenException;
import com.example.SonicCanopy.domain.exception.auth.RefreshTokenExpiredException;
import com.example.SonicCanopy.service.app.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticate(request);
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return generateTokens(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        try {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails user = userService.loadUserByUsername(username);

            if (!jwtService.validateToken(refreshToken, user)) {
                throw new InvalidRefreshTokenException("Refresh token is invalid or tampered");
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            return new AuthResponse(newAccessToken, refreshToken);

        } catch (ExpiredJwtException e) {
            throw new RefreshTokenExpiredException("Refresh token has expired");
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }
    }

    private Authentication authenticate(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("User '{}' successfully authenticated", request.username());
            return authentication;
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    private AuthResponse generateTokens(UserDetails user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        log.debug("Generated access and refresh tokens for user '{}'", user.getUsername());
        return new AuthResponse(accessToken, refreshToken);
    }
}
