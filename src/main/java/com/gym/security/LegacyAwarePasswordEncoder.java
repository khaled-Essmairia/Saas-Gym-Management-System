package com.gym.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * The PHP project stored passwords as plain MD5 hashes.
 * <ul>
 *   <li>Existing MD5 hashes are still accepted, so all old accounts keep working.</li>
 *   <li>New or changed passwords are stored with BCrypt.</li>
 *   <li>{@link #upgradeEncoding} lets Spring Security re-hash an MD5 password
 *       with BCrypt the first time the user logs in.</li>
 * </ul>
 */
public class LegacyAwarePasswordEncoder implements PasswordEncoder {

    private static final Pattern MD5_HEX = Pattern.compile("^[a-fA-F0-9]{32}$");

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        if (isMd5(encodedPassword)) {
            byte[] expected = md5Hex(rawPassword.toString()).getBytes(StandardCharsets.US_ASCII);
            byte[] actual = encodedPassword.toLowerCase().getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(expected, actual);
        }
        if (encodedPassword.startsWith("$2")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        return false;
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return encodedPassword != null && isMd5(encodedPassword);
    }

    static boolean isMd5(String value) {
        return MD5_HEX.matcher(value).matches();
    }

    static String md5Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
