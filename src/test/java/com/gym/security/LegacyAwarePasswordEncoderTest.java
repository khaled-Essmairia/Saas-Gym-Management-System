package com.gym.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Runs without a database: mvn test */
class LegacyAwarePasswordEncoderTest {

    private final LegacyAwarePasswordEncoder encoder = new LegacyAwarePasswordEncoder();

    @Test
    void acceptsTheMd5HashesFromThePhpDatabase() {
        // admin / codeastro.com and the demo members/staff / codeastro (from gymnsb.sql)
        assertTrue(encoder.matches("codeastro.com", "f2d0ff370380124029c2b807a924156c"));
        assertTrue(encoder.matches("codeastro", "cac29d7a34687eb14b37068ee4708e7b"));
        assertFalse(encoder.matches("wrong", "cac29d7a34687eb14b37068ee4708e7b"));
    }

    @Test
    void newPasswordsUseBcrypt() {
        String hash = encoder.encode("secret");
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("secret", hash));
        assertFalse(encoder.matches("other", hash));
        assertFalse(encoder.upgradeEncoding(hash));
    }

    @Test
    void md5HashesAreUpgradedOnLogin() {
        assertTrue(encoder.upgradeEncoding("cac29d7a34687eb14b37068ee4708e7b"));
    }

    @Test
    void rejectsEmptyValues() {
        assertFalse(encoder.matches("x", null));
        assertFalse(encoder.matches("x", ""));
        assertFalse(encoder.matches(null, "cac29d7a34687eb14b37068ee4708e7b"));
    }
}
