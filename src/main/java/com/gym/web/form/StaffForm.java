package com.gym.web.form;

import jakarta.validation.constraints.*;

/** Staff entry / update form (staffs-entry.php, edit-staff-form.php). */
public class StaffForm {

    @NotBlank(message = "Full name is required")
    @Size(max = 50, message = "Full name: max 50 characters")
    private String fullname;

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username: max 50 characters")
    private String username;

    /** Required when creating, ignored when updating. */
    @Size(max = 72, message = "Password is too long")
    private String password;

    private String password2;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    @Size(max = 50, message = "Email: max 50 characters")
    private String email;

    @NotBlank(message = "Address is required")
    @Size(max = 20, message = "Address: max 20 characters")
    private String address;

    @NotBlank
    @Size(max = 20)
    private String designation = "Cashier";

    @NotBlank
    @Size(max = 10)
    private String gender = "Male";

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{6,15}", message = "Contact: digits only (6 to 15)")
    private String contact;

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPassword2() { return password2; }
    public void setPassword2(String password2) { this.password2 = password2; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}
