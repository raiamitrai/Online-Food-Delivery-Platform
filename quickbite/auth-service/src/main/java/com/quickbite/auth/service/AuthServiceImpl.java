package com.quickbite.auth.service;

import com.quickbite.auth.dto.AuthRequest;
import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.dto.UserResponse;
import com.quickbite.auth.entity.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.quickbite.auth.repository.UserRepository;
import com.quickbite.auth.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RestTemplate restTemplate;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.restTemplate = restTemplate;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("User registered successfully")
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        System.out.println("Login attempt for email: " + request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            System.out.println("Authentication failed for: " + request.getEmail() + " - " + e.getMessage());
            throw e;
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Send Welcome Notification
        try {
            Map<String, Object> welcomeNote = new HashMap<>();
            welcomeNote.put("customerId", user.getId());
            welcomeNote.put("type", "SYSTEM");
            welcomeNote.put("message", "Welcome back, " + user.getName() + "! Hope you're hungry today.");
            restTemplate.postForEntity("http://notification-service/api/notifications", welcomeNote, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send welcome notification: " + e.getMessage());
        }

        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("Login successful")
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        if (username != null) {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtUtil.isTokenValid(refreshToken, user.getEmail())) {
                String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
                return AuthResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken)
                        .message("Token refreshed successfully")
                        .build();
            }
        }
        throw new RuntimeException("Invalid refresh token");
    }
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .build())
                .collect(Collectors.toList());
    }
}
