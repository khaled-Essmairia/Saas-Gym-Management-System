package com.gym.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Logged-in user. Replaces the PHP $_SESSION['user_id'].
 * {@link #getId()} is the user_id of the admin, staff or member row.
 */
public class GymUser extends User {

    private final Integer id;
    private final String displayName;
    private final String area;

    public GymUser(Integer id, String username, String password, String displayName,
                   String area, boolean enabled) {
        super(username, password, enabled, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + area.toUpperCase())));
        this.id = id;
        this.displayName = displayName;
        this.area = area;
    }

    public Integer getId() { return id; }
    public String getDisplayName() { return displayName; }
    /** "admin", "staff" or "customer". */
    public String getArea() { return area; }
}
