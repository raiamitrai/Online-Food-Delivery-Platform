package com.quickbite.auth.service;

import com.quickbite.auth.dto.AuthRequest;
import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.entity.Role;
import com.quickbite.auth.entity.User;
import com.quickbite.auth.repository.UserRepository;
import com.quickbite.auth.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    public void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@test.com");
        request.setPassword("pass123");
        request.setRole(Role.CUSTOMER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPass");
        when(jwtUtil.generateToken(any(), any())).thenReturn("mockToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("mockRefreshToken");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockToken", response.getAccessToken());
        assertEquals("mockRefreshToken", response.getRefreshToken());
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testLogin_Success() {
        // Arrange
        AuthRequest request = new AuthRequest();
        request.setEmail("alice@test.com");
        request.setPassword("pass123");

        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@test.com")
                .role(Role.CUSTOMER)
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any())).thenReturn("mockToken");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("mockRefreshToken");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockToken", response.getAccessToken());
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }
}
