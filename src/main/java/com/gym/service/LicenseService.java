package com.gym.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.entity.License;
import com.gym.repository.LicenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

/**
 * Checks the license keys made by the license generator tool and keeps them in the database.
 *
 * A key looks like  base64url(JSON) "." base64url(ECDSA signature).
 * The JSON holds the end date ("exp", yyyy-MM-dd, that day included) and an optional client name.
 * Only the PUBLIC key is in this application, so a license can be checked here but not created.
 *
 * A key can also be tied to one computer: if its JSON has a "mid" (Machine ID, see MachineId), the key
 * is only valid on the computer that shows that ID, so copying the application and its database to
 * another computer does not work. Keys without "mid" keep working unless gym.license.require-machine-id=true.
 *
 * The signature and the end date are checked again on every call to status(), not only when the
 * key is saved, so an expired license, a missing license, or a row edited by hand in the database
 * all result in "not valid".
 */
@Service
public class LicenseService {

    private static final Logger log = LoggerFactory.getLogger(LicenseService.class);

    private final LicenseRepository repository;
    private final GymClock clock;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PublicKey publicKey;
    private final boolean requireMachineId;

    public LicenseService(LicenseRepository repository, GymClock clock,
                          @Value("${gym.license.public-key:}") String publicKeyBase64,
                          @Value("${gym.license.require-machine-id:false}") boolean requireMachineId) {
        this.repository = repository;
        this.clock = clock;
        this.publicKey = parsePublicKey(publicKeyBase64);
        this.requireMachineId = requireMachineId;
    }

    /** Machine ID of this computer, shown in License Management so the client can send it to you. */
    public String machineId() {
        return MachineId.current();
    }

    /** State of the license stored in the database, checked against today's date. */
    public Status status() {
        return repository.findFirstByOrderByIdDesc()
                .map(row -> check(row.getLicenseKey()))
                .orElseGet(() -> Status.invalid("No license has been entered yet."));
    }

    /** The last licenses that were entered, newest first. */
    public List<License> history() {
        return repository.findTop10ByOrderByIdDesc();
    }

    /** Checks the key and, only if it is valid today, stores it in the database as the active (newest) license. */
    public Status activate(String rawKey) {
        Status result = check(rawKey);
        if (result.isValid()) {
            License row = new License();
            row.setLicenseKey(clean(rawKey));
            row.setCustomer(result.getCustomer());
            row.setExpiresOn(result.getExpires());
            row.setActivatedAt(clock.now());
            repository.save(row);
        }
        return result;
    }

    /** Verifies the signature and the end date of a key. Never throws. */
    public Status check(String rawKey) {
        if (publicKey == null) {
            return Status.invalid("License checking is not set up (gym.license.public-key is missing).");
        }
        try {
            String[] parts = clean(rawKey).split("\\.");
            if (parts.length != 2) {
                return Status.invalid("This is not a valid license key.");
            }

            Signature verifier = Signature.getInstance("SHA256withECDSAinP1363Format");
            verifier.initVerify(publicKey);
            verifier.update(parts[0].getBytes(StandardCharsets.US_ASCII));
            if (!verifier.verify(Base64.getUrlDecoder().decode(parts[1]))) {
                return Status.invalid("This is not a valid license key.");
            }

            JsonNode payload = mapper.readTree(Base64.getUrlDecoder().decode(parts[0]));
            LocalDate expires = LocalDate.parse(payload.path("exp").asText());
            String customer = payload.path("name").asText("");

            String boundTo = payload.path("mid").asText("").trim();
            boolean bound = !boundTo.isEmpty();
            if (bound && !boundTo.equalsIgnoreCase(MachineId.current())) {
                return Status.invalid("This license was issued for another computer. "
                        + "Send the Machine ID shown on this page to your provider to get a license for this computer.");
            }
            if (!bound && requireMachineId) {
                return Status.invalid("This license is not tied to a computer. "
                        + "Send the Machine ID shown on this page to your provider to get a new license.");
            }

            if (clock.today().isAfter(expires)) {
                return Status.expired(expires, customer);
            }
            return Status.valid(expires, customer, bound);
        } catch (Exception e) {
            return Status.invalid("This is not a valid license key.");
        }
    }

    private static String clean(String rawKey) {
        return rawKey == null ? "" : rawKey.replaceAll("\\s+", "");
    }

    private static PublicKey parsePublicKey(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        try {
            byte[] der = Base64.getMimeDecoder().decode(base64.trim());
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            log.error("gym.license.public-key is not a valid public key", e);
            return null;
        }
    }

    /** Result of a license check. Plain getters so Thymeleaf can use ${license.valid}. */
    public static final class Status {
        private final boolean valid;
        private final boolean expired;
        private final String message;
        private final LocalDate expires;
        private final String customer;
        private final boolean bound;

        private Status(boolean valid, boolean expired, String message, LocalDate expires, String customer, boolean bound) {
            this.valid = valid;
            this.expired = expired;
            this.message = message;
            this.expires = expires;
            this.customer = customer;
            this.bound = bound;
        }

        static Status valid(LocalDate expires, String customer, boolean bound) {
            return new Status(true, false, "License valid until " + expires + ".", expires, customer, bound);
        }

        static Status expired(LocalDate expires, String customer) {
            return new Status(false, true, "This license expired on " + expires + ".", expires, customer, false);
        }

        static Status invalid(String message) {
            return new Status(false, false, message, null, "", false);
        }

        public boolean isValid() { return valid; }
        public boolean isExpired() { return expired; }
        public String getMessage() { return message; }
        public LocalDate getExpires() { return expires; }
        public String getCustomer() { return customer; }
        /** true when the key is tied to this computer. */
        public boolean isBound() { return bound; }
    }
}
