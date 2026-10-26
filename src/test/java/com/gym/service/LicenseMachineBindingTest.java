package com.gym.service;

import com.gym.entity.License;
import com.gym.repository.LicenseRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Licenses are signed here exactly like license-generator.html does (ECDSA P-256, raw r||s). */
class LicenseMachineBindingTest {

    private static KeyPair keys;
    private static final String EXPIRY = "2027-01-01";

    @BeforeAll
    static void createKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(256);
        keys = generator.generateKeyPair();
    }

    private static String sign(String json) throws Exception {
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        Signature signature = Signature.getInstance("SHA256withECDSAinP1363Format");
        signature.initSign(keys.getPrivate());
        signature.update(payload.getBytes(StandardCharsets.US_ASCII));
        return payload + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
    }

    private static LicenseService service(LicenseRepository repository, boolean requireMachineId) {
        GymClock clock = mock(GymClock.class);
        when(clock.today()).thenReturn(LocalDate.of(2026, 10, 26));
        when(clock.now()).thenReturn(LocalDateTime.of(2026, 10, 26, 10, 0));
        String publicKey = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
        return new LicenseService(repository, clock, publicKey, requireMachineId);
    }

    private static String otherComputer() {
        return MachineId.current().equals("0000-0000-0000-0000") ? "1111-1111-1111-1111" : "0000-0000-0000-0000";
    }

    @Test
    void keyWithoutComputerIdStillWorksByDefault() throws Exception {
        String key = sign("{\"v\":1,\"exp\":\"" + EXPIRY + "\",\"name\":\"Perfect GYM\"}");
        LicenseService.Status status = service(mock(LicenseRepository.class), false).check(key);

        assertTrue(status.isValid());
        assertFalse(status.isBound());
    }

    @Test
    void keyWithoutComputerIdIsRefusedWhenComputerIdIsRequired() throws Exception {
        String key = sign("{\"v\":1,\"exp\":\"" + EXPIRY + "\"}");
        assertFalse(service(mock(LicenseRepository.class), true).check(key).isValid());
    }

    @Test
    void keyForThisComputerIsValidAndBound() throws Exception {
        String key = sign("{\"v\":1,\"exp\":\"" + EXPIRY + "\",\"mid\":\"" + MachineId.current() + "\"}");
        LicenseService.Status status = service(mock(LicenseRepository.class), true).check(key);

        assertTrue(status.isValid());
        assertTrue(status.isBound());
    }

    @Test
    void keyForAnotherComputerIsRefusedAndNotSaved() throws Exception {
        String key = sign("{\"v\":1,\"exp\":\"" + EXPIRY + "\",\"mid\":\"" + otherComputer() + "\"}");
        LicenseRepository repository = mock(LicenseRepository.class);

        LicenseService.Status status = service(repository, false).activate(key);

        assertFalse(status.isValid());
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void storedKeyOfAnotherComputerIsRefusedOnEveryCheck() throws Exception {
        // what a copied database looks like on the new computer
        License row = new License();
        row.setLicenseKey(sign("{\"v\":1,\"exp\":\"" + EXPIRY + "\",\"mid\":\"" + otherComputer() + "\"}"));
        LicenseRepository repository = mock(LicenseRepository.class);
        when(repository.findFirstByOrderByIdDesc()).thenReturn(Optional.of(row));

        assertFalse(service(repository, false).status().isValid());
    }

    @Test
    void changingTheComputerIdInsideTheKeyBreaksTheSignature() throws Exception {
        String original = sign("{\"v\":1,\"exp\":\"" + EXPIRY + "\",\"mid\":\"" + otherComputer() + "\"}");
        String signature = original.split("\\.")[1];
        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                ("{\"v\":1,\"exp\":\"" + EXPIRY + "\",\"mid\":\"" + MachineId.current() + "\"}").getBytes(StandardCharsets.UTF_8));

        assertFalse(service(mock(LicenseRepository.class), false).check(forgedPayload + "." + signature).isValid());
    }
}
