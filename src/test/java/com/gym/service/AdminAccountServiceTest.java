package com.gym.service;

import com.gym.entity.Admin;
import com.gym.repository.AdminRepository;
import com.gym.security.LegacyAwarePasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminAccountServiceTest {

    private final LegacyAwarePasswordEncoder encoder = new LegacyAwarePasswordEncoder();
    private AdminRepository admins;
    private AdminAccountService service;
    private Admin admin;

    @BeforeEach
    void setUp() throws Exception {
        admins = mock(AdminRepository.class);
        admin = new Admin();
        admin.setUserId(2);
        admin.setUsername("admin");
        // old PHP accounts store a plain MD5 hash
        admin.setPassword(HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest("oldpass1".getBytes(StandardCharsets.UTF_8))));
        when(admins.findById(2)).thenReturn(Optional.of(admin));
        service = new AdminAccountService(admins, encoder);
    }

    @Test
    void changesThePasswordAndStoresItWithBCrypt() {
        assertNull(service.changePassword(2, "oldpass1", "newpass99", "newpass99"));

        verify(admins).save(admin);
        assertTrue(admin.getPassword().startsWith("$2"));
        assertTrue(encoder.matches("newpass99", admin.getPassword()));
        assertTrue(!encoder.matches("oldpass1", admin.getPassword()));
    }

    @Test
    void wrongOldPasswordChangesNothing() {
        String before = admin.getPassword();
        assertNotNull(service.changePassword(2, "wrong", "newpass99", "newpass99"));
        assertNotNull(service.changePassword(2, "", "newpass99", "newpass99"));
        assertEquals(before, admin.getPassword());
        verify(admins, never()).save(any());
    }

    @Test
    void refusesWeakMismatchedOrUnchangedPasswords() {
        assertNotNull(service.changePassword(2, "oldpass1", "abc", "abc"));
        assertNotNull(service.changePassword(2, "oldpass1", "newpass99", "newpass98"));
        assertNotNull(service.changePassword(2, "oldpass1", "oldpass1", "oldpass1"));
        assertNotNull(service.changePassword(2, "oldpass1", "x".repeat(73), "x".repeat(73)));
        verify(admins, never()).save(any());
    }

    @Test
    void unknownAdminIsRefused() {
        assertNotNull(service.changePassword(99, "oldpass1", "newpass99", "newpass99"));
    }
}
