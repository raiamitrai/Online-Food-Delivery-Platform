package com.quickbite.auth.repository;

import com.quickbite.auth.entity.Role;
import com.quickbite.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail() {
        // Arrange
        User user = User.builder()
                .name("John")
                .email("john@test.com")
                .password("password123")
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("john@test.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John");
    }

    @Test
    public void testExistsByEmail() {
        // Arrange
        User user = User.builder()
                .name("Jane")
                .email("jane@test.com")
                .password("password123")
                .role(Role.OWNER)
                .build();
        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByEmail("jane@test.com");
        boolean notExists = userRepository.existsByEmail("nobody@test.com");

        // Assert
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
