package com.gym.security;

import com.gym.entity.Member;
import com.gym.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Login against the "members" table (replaces customer/index.php).
 * Only members with status "Active" may log in; others are reported as
 * disabled ("Account has been expired").
 */
@Service
public class CustomerUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final MemberRepository members;

    public CustomerUserDetailsService(MemberRepository members) {
        this.members = members;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return toUser(find(username));
    }

    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        Member m = members.findById(((GymUser) user).getId()).orElseThrow();
        m.setPassword(newPassword);
        members.save(m);
        return toUser(m);
    }

    /** The old data has duplicate usernames: prefer an active account. */
    private Member find(String username) {
        List<Member> found = members.findByUsernameOrderByUserId(username);
        if (found.isEmpty()) {
            throw new UsernameNotFoundException("Unknown member");
        }
        return found.stream()
                .filter(m -> Member.ACTIVE.equals(m.getStatus()))
                .findFirst()
                .orElse(found.get(0));
    }

    private GymUser toUser(Member m) {
        return new GymUser(m.getUserId(), m.getUsername(), m.getPassword(), m.getFullname(), "customer",
                Member.ACTIVE.equals(m.getStatus()));
    }
}
