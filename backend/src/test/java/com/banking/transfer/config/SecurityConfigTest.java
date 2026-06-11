package com.banking.transfer.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for SecurityConfig to verify bean creation and password encoding.
 */
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    @DisplayName("passwordEncoder bean should be created and use BCrypt")
    void passwordEncoder_shouldBeBCrypt() {
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder.getClass().getSimpleName()).isEqualTo("BCryptPasswordEncoder");
    }

    @Test
    @DisplayName("passwordEncoder should encode passwords correctly")
    void passwordEncoder_shouldEncodePasswords() {
        String rawPassword = "TestPassword123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).isNotNull();
        assertThat(encoded).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword", encoded)).isFalse();
    }

    @Test
    @DisplayName("securityFilterChain bean should be created")
    void securityFilterChain_shouldBeCreated() {
        assertThat(securityFilterChain).isNotNull();
    }
}
