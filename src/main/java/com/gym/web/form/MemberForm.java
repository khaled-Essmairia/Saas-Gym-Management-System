package com.gym.web.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Member entry / update form (member-entry.php, edit-memberform.php).
 * Column sizes follow the original "members" table.
 */
public class MemberForm {

    @NotBlank(message = "Full name is required")
    @Size(max = 20, message = "Full name: max 20 characters")
    private String fullname;

    @NotBlank(message = "Username is required")
    @Size(max = 20, message = "Username: max 20 characters")
    private String username;

    /** Only used when creating a member. */
    @Size(max = 72, message = "Password is too long")
    private String password;

    @NotBlank
    private String gender = "Male";

    @NotNull(message = "Date of registration is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dor;

    @NotNull
    private Integer plan = 1;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{6,15}", message = "Contact: digits only (6 to 15)")
    private String contact;

    @NotBlank(message = "Address is required")
    @Size(max = 20, message = "Address: max 20 characters")
    private String address;

    @NotBlank(message = "Please choose a service")
    private String services;

    /** Amount per month. Stored amount = amount x plan (same as PHP). */
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private Integer amount;

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getDor() { return dor; }
    public void setDor(LocalDate dor) { this.dor = dor; }
    public Integer getPlan() { return plan; }
    public void setPlan(Integer plan) { this.plan = plan; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}
