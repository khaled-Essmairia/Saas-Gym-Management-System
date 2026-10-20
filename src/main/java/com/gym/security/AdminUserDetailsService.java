package com.gym.security;

import com.gym.entity.Admin;
import com.gym.repository.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login against the "admin" table (replaces the root index.php login). */
@Service
public class AdminUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final AdminRepository admins;

    public AdminUserDetailsService(AdminRepository admins) {
        this.admins = admins;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Admin a = admins.findFirstByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown admin"));
        return toUser(a);
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        Admin a = admins.findFirstByUsername(user.getUsername()).orElseThrow();
        a.setPassword(newPassword);
        admins.save(a);
        return toUser(a);
    }

    private GymUser toUser(Admin a) {
        return new GymUser(a.getUserId(), a.getUsername(), a.getPassword(), a.getName(), "admin", true);
    }
}
