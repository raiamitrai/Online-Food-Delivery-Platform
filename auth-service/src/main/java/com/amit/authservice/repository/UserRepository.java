package com.amit.authservice.repository;

import com.amit.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔹 Login / Auth
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmailOrPhone(String email, String phone); // 🔥 IMPORTANT

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone); // 🔥 add this

    // 🔹 Optional (future use)
    List<User> findAllByRole(String role);

    List<User> findByFullNameContaining(String name);
}