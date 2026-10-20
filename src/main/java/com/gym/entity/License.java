package com.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** A license key that was entered in License Management. The newest row is the active license. */
@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "license_key")
    private String licenseKey;

    @Column(name = "customer")
    private String customer;

    @Column(name = "expires_on")
    private LocalDate expiresOn;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLicenseKey() { return licenseKey; }
    public void setLicenseKey(String licenseKey) { this.licenseKey = licenseKey; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public LocalDate getExpiresOn() { return expiresOn; }
    public void setExpiresOn(LocalDate expiresOn) { this.expiresOn = expiresOn; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }
}
