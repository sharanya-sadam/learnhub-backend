package com.learnhub.service;

import com.learnhub.dto.auth.AuthResponse;
import com.learnhub.dto.auth.LoginRequest;
import com.learnhub.dto.auth.RegisterRequest;
import com.learnhub.entity.Role;
import com.learnhub.entity.User;
import com.learnhub.exception.BadRequestException;
import com.learnhub.repository.UserRepository;
import com.learnhub.security.CustomUserDetails;
import com.learnhub.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Test Student")
                .email("student@learnhub.com")
                .passwordHash("hashedPassword")
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new student and generate JWT")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test Student")
                .email("student@learnhub.com")
                .password("Password123")
                .role(Role.ROLE_STUDENT)
                .build();

        when(userRepository.existsByEmail("student@learnhub.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateToken(any(CustomUserDetails.class), eq(1L), eq("ROLE_STUDENT")))
                .thenReturn("mocked.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("student@learnhub.com", response.getEmail());
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals(Role.ROLE_STUDENT, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when email is already registered")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Duplicate User")
                .email("student@learnhub.com")
                .password("Password123")
                .build();

        when(userRepository.existsByEmail("student@learnhub.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject public registration attempting to assign ROLE_ADMIN")
    void testRegisterAdminRoleRejected() {
        RegisterRequest request = RegisterRequest.builder()
                .name("Hacker")
                .email("hacker@learnhub.com")
                .password("Password123")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userRepository.existsByEmail("hacker@learnhub.com")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should authenticate valid user and return JWT token")
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("student@learnhub.com")
                .password("Password123")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(sampleUser);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(CustomUserDetails.class), eq(1L), eq("ROLE_STUDENT")))
                .thenReturn("mocked.jwt.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("student@learnhub.com", response.getEmail());
        assertEquals("mocked.jwt.token", response.getToken());
    }

    @Test
    @DisplayName("Should throw BadRequestException when credentials are invalid")
    void testLoginBadCredentials() {
        LoginRequest request = LoginRequest.builder()
                .email("student@learnhub.com")
                .password("WrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid"));

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }
}
