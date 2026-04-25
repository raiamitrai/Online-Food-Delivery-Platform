package com.amit.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;


@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotBlank(message = "Name is required")
    private String fullName;


    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;


    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String passwordHash;

    @NotBlank(message = "Phone is required")
    private String phone;

   @Enumerated(EnumType.STRING)
    private Role role; // CUSTOMER / OWNER / AGENT / ADMIN

    private String provider;

    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String profilePicUrl;
}