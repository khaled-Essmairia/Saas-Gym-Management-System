package com.gym.web.form;

import jakarta.validation.constraints.*;

/** "Join Now!" form on the customer login page (register-cust.php). */
public class RegistrationForm {

    @NotBlank(message = "Full name is required")
    @Size(max = 20, message = "Full name: max 20 characters")
    private String fullname;

    @NotBlank(message = "Username is required")
    @Size(max = 20, message = "Username: max 20 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 72, message = "Password: 4 to 72 characters")
    private String password;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{6,15}", message = "Contact: digits only (6 to 15)")
    private String contact;

    @NotBlank(message = "Address is required")
    @Size(max = 20, message = "Address: max 20 characters")
    private String address;

    @NotBlank
    private String gender = "Male";

    @NotNull(message = "Please select a plan")
    private Integer plan;

    @NotBlank(message = "Please select a service")
    private String services;

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getPlan() { return plan; }
    public void setPlan(Integer plan) { this.plan = plan; }
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
}
