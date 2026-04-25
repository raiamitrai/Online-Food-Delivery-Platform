package com.amit.authservice.controller;

import com.amit.authservice.dto.LoginRequest;
import com.amit.authservice.dto.UserResponse;
import com.amit.authservice.entity.User;
import com.amit.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

    private final AuthService service;

    //  REGISTER
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody User user) {

        User savedUser = service.register(user);

        UserResponse response = new UserResponse(
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhone()
        );

        return ResponseEntity.ok(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        return ResponseEntity.ok(
                service.login(req.getLogin(), req.getPassword())
        );
    }

    //  LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader String token) {
        service.logout(token);
        return ResponseEntity.ok("Logged out");
    }

    //  REFRESH
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String header) {

        String token = header.replace("Bearer ", "");

        return ResponseEntity.ok(service.refreshToken(token));
    }


    @GetMapping("/profile/{id}")
    public ResponseEntity<UserResponse> profile(@PathVariable Long id) {

        User user = service.getUserById(id);

        UserResponse response = new UserResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone()
        );

        return ResponseEntity.ok(response);
    }

    // 🔹 UPDATE PROFILE
    @PutMapping("/profile/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody User user) {

        User updated = service.updateProfile(id, user);

        UserResponse response = new UserResponse(
                updated.getUserId(),
                updated.getFullName(),
                updated.getEmail(),
                user.getPhone()
        );

        return ResponseEntity.ok(response);
    }

    //  CHANGE PASSWORD
    @PostMapping("/password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
                                            @RequestBody Map<String, String> req) {
        service.changePassword(id, req.get("password"));
        return ResponseEntity.ok("Password Updated");
    }

    //  DEACTIVATE
    @DeleteMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        service.deactivateAccount(id);
        return ResponseEntity.ok("Account Deactivated");
    }
}