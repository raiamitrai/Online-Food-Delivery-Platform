package com.quickbite.auth.service;

import com.quickbite.auth.dto.AuthRequest;
import com.quickbite.auth.dto.AuthResponse;
import com.quickbite.auth.dto.RegisterRequest;
import com.quickbite.auth.dto.UserResponse;
import java.util.List;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);
    List<UserResponse> getAllUsers();
}
