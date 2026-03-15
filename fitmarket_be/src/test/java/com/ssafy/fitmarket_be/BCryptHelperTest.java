package com.ssafy.fitmarket_be;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptHelperTest {

    @Test
    void printBCryptHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi";
        String newHash = "$2a$10$2PPDxpUKhq3FipmhvcSEIeQLhKArl2I3gP2LMoehOa/J2JTLT8DoW";
        System.out.println("OLD HASH:");
        System.out.println("'password' matches: " + encoder.matches("password", oldHash));
        System.out.println("'password123' matches: " + encoder.matches("password123", oldHash));
        System.out.println("NEW HASH:");
        System.out.println("'password' matches: " + encoder.matches("password", newHash));
        System.out.println("'password123' matches: " + encoder.matches("password123", newHash));
        System.out.println("New hash for 'password123': " + encoder.encode("password123"));
    }
}
