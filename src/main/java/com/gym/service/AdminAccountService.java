package com.gym.service;

import com.gym.entity.Admin;
import com.gym.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Password change of the logged-in admin (System Administration > Admin Profile). */
@Service
@Transactional
public class AdminAccountService {

    public static final int MIN_LENGTH = 6;
    /** BCrypt only uses the first 72 bytes of a password. */
    public static final int MAX_LENGTH = 72;

    private final AdminRepository admins;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(AdminRepository admins, PasswordEncoder passwordEncoder) {
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Changes the password of the admin with this id.
     *
     * @return null when the password was changed, otherwise the message to show to the admin
     */
    public String changePassword(Integer adminId, String oldPassword, String newPassword, String confirmPassword) {
        Admin admin = admins.findById(adminId).orElse(null);
        if (admin == null) {
            return "This admin account no longer exists.";
        }
        if (oldPassword == null || oldPassword.isEmpty()
                || !passwordEncoder.matches(oldPassword, admin.getPassword())) {
            return "The old password is not correct.";
        }
        if (newPassword == null || newPassword.length() < MIN_LENGTH) {
            return "The new password must have at least " + MIN_LENGTH + " characters.";
        }
        if (newPassword.length() > MAX_LENGTH) {
            return "The new password must have at most " + MAX_LENGTH + " characters.";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "The new password and its confirmation are not the same.";
        }
        if (newPassword.equals(oldPassword)) {
            return "The new password must be different from the old one.";
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        admins.save(admin);
        return null;
    }
}
