package com.doccshare.user;

import com.doccshare.common.exception.ConflictException;
import com.doccshare.common.exception.UnauthorizedException;
import com.doccshare.security.JwtService;
import com.doccshare.user.dto.AuthResponse;
import com.doccshare.user.dto.LoginRequest;
import com.doccshare.user.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("newuser@example.com", "password123", "New User");

        when(userRepository.existsByEmailIgnoreCase("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(userId);
            u.setCreatedAt(Instant.now());
            return u;
        });
        when(jwtService.generateToken(userId, "newuser@example.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("newuser@example.com", response.getUser().getEmail());
        assertEquals("New User", response.getUser().getDisplayName());
    }

    @Test
    void register_emailAlreadyExists_throwsConflict() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "User");
        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .displayName("Test User")
                .passwordHash("hashed-pw")
                .createdAt(Instant.now())
                .build();

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);
        when(jwtService.generateToken(userId, "user@example.com")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("user@example.com", response.getUser().getEmail());
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        User user = User.builder()
                .id(userId)
                .email("user@example.com")
                .passwordHash("hashed-pw")
                .build();

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-pw")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void login_userNotFound_throwsUnauthorized() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}

