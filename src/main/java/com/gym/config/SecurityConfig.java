package com.gym.config;

import com.gym.security.AdminUserDetailsService;
import com.gym.security.CustomerUserDetailsService;
import com.gym.security.LegacyAwarePasswordEncoder;
import com.gym.security.StaffUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Three separate login areas, like the PHP project:
 * <pre>
 *   /admin/**    -> table "admin"    (old: index.php)
 *   /staff/**    -> table "staffs"   (old: staff/index.php)
 *   /customer/** -> table "members"  (old: customer/index.php, Active members only)
 * </pre>
 * Each area has its own login page, its own user table and its own role.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new LegacyAwarePasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity http, AdminUserDetailsService users,
                                          PasswordEncoder encoder) throws Exception {
        return areaChain(http, "admin", "ADMIN", users, users, encoder);
    }

    @Bean
    @Order(2)
    public SecurityFilterChain staffChain(HttpSecurity http, StaffUserDetailsService users,
                                          PasswordEncoder encoder) throws Exception {
        return areaChain(http, "staff", "STAFF", users, users, encoder);
    }

    @Bean
    @Order(3)
    public SecurityFilterChain customerChain(HttpSecurity http, CustomerUserDetailsService users,
                                             PasswordEncoder encoder) throws Exception {
        return areaChain(http, "customer", "CUSTOMER", users, users, encoder);
    }

    /** Everything else: home page redirect, static files, error page. */
    @Bean
    @Order(4)
    public SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    private SecurityFilterChain areaChain(HttpSecurity http, String area, String role,
                                          UserDetailsService users,
                                          UserDetailsPasswordService passwordUpdater,
                                          PasswordEncoder encoder) throws Exception {
        String base = "/" + area;
        http.securityMatcher(base + "/**")
            .authenticationManager(authenticationManager(users, passwordUpdater, encoder))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(base + "/login", base + "/register").permitAll()
                    .anyRequest().hasRole(role))
            .formLogin(form -> form
                    .loginPage(base + "/login")
                    .loginProcessingUrl(base + "/login")
                    .defaultSuccessUrl(base + "/dashboard", true)
                    .failureUrl(base + "/login?error")
                    .permitAll())
            .logout(logout -> logout
                    .logoutUrl(base + "/logout")
                    .logoutSuccessUrl(base + "/login?logout")
                    .permitAll())
            // Logged in, but as another kind of user (e.g. a customer opening /admin)
            .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, denied) ->
                    response.sendRedirect(request.getContextPath() + base + "/login?denied")));
        return http.build();
    }

    @SuppressWarnings("deprecation")
    private AuthenticationManager authenticationManager(UserDetailsService users,
                                                        UserDetailsPasswordService passwordUpdater,
                                                        PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(users);
        provider.setUserDetailsPasswordService(passwordUpdater);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}
