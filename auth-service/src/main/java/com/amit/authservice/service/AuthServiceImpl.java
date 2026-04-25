package com.amit.authservice.service;

import com.amit.authservice.dto.LoginResponse;
import com.amit.authservice.entity.Role;
import com.amit.authservice.entity.User;
import com.amit.authservice.repository.UserRepository;
import com.amit.authservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    // 🔹 REGISTER
    @Override
    public User register(User user) {

        if (repo.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (repo.existsByPhone(user.getPhone())) {
            throw new RuntimeException("Phone already exists");
        }

        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        user.setPasswordHash(encoder.encode(user.getPasswordHash()));

        return repo.save(user);
    }

    // 🔹 LOGIN (Email OR Phone)
    @Override
    public LoginResponse login(String input, String password) {

        User user = repo.findByEmailOrPhone(input, input)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name()
        );
    }

    // 🔹 LOGOUT
    @Override
    public void logout(String token) {
        // Stateless → nothing
    }

    // 🔹 VALIDATE TOKEN
    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    // 🔹 REFRESH TOKEN
   @Override
    public String refreshToken(String token) {

        String email = jwtUtil.extractUsername(token);

        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
    }

    // 🔹 GET USER BY EMAIL
    @Override
    public User getUserByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔹 GET USER BY ID
    @Override
    public User getUserById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔹 UPDATE PROFILE
    @Override
    public User updateProfile(Long id, User newUser) {

        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(newUser.getFullName());
        user.setPhone(newUser.getPhone());
        user.setProfilePicUrl(newUser.getProfilePicUrl());

        return repo.save(user);
    }

    // 🔹 CHANGE PASSWORD
    @Override
    public void changePassword(Long id, String newPassword) {

        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(encoder.encode(newPassword));

        repo.save(user);
    }

    // 🔹 DEACTIVATE ACCOUNT
    @Override
    public void deactivateAccount(Long id) {

        User user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);

        repo.save(user);
    }
}