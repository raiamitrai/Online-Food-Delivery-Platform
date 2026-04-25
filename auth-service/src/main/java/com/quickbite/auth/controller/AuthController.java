package com.quickbite.auth.controller;

import com.quickbite.auth.dto.AuthRequest;
import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.quickbite.auth.dto.UserResponse;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        if(refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        }
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }
}
