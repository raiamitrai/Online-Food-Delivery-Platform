package com.amit.authservice.service;

import com.amit.authservice.dto.LoginResponse;
import com.amit.authservice.entity.User;

public interface AuthService {

    User register(User user);

    LoginResponse login(String email, String password);

    void logout(String token);

    boolean validateToken(String token);

    String refreshToken(String token);

    User getUserByEmail(String email);

    User getUserById(Long id);

    User updateProfile(Long id, User user);

    void changePassword(Long id, String newPassword);

    void deactivateAccount(Long id);
}