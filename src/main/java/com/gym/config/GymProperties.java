package com.gym.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

/** Settings from application.properties (prefix "gym"). Available in templates as ${gym}. */
@ConfigurationProperties(prefix = "gym")
public class GymProperties {

    private String timezone;
    private String name = "Perfect GYM Club";
    private String address = "5021 Wetzel Lane, Williamsburg";
    private String phone = "231-267-6011";
    private String email = "support@perfectgym.com";

    public ZoneId zone() {
        return (timezone == null || timezone.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(timezone);
    }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
