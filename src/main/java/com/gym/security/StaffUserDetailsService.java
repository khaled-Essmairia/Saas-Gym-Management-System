package com.gym.security;

import com.gym.entity.Staff;
import com.gym.repository.StaffRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login against the "staffs" table (replaces staff/index.php). */
@Service
public class StaffUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final StaffRepository staffs;

    public StaffUserDetailsService(StaffRepository staffs) {
        this.staffs = staffs;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Staff s = staffs.findFirstByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown staff"));
        return toUser(s);
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        Staff s = staffs.findFirstByUsername(user.getUsername()).orElseThrow();
        s.setPassword(newPassword);
        staffs.save(s);
        return toUser(s);
    }

    private GymUser toUser(Staff s) {
        return new GymUser(s.getUserId(), s.getUsername(), s.getPassword(), s.getFullname(), "staff", true);
    }
}
