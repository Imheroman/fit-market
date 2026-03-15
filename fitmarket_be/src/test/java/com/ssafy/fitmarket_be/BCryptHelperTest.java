package com.ssafy.fitmarket_be;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptHelperTest {

    @Test
    void printBCryptHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi";
        System.out.println("'password' matches: " + encoder.matches("password", oldHash));
        System.out.println("'password123' matches: " + encoder.matches("password123", oldHash));
        System.out.println("New hash for 'password123': " + encoder.encode("password123"));
    }
}
