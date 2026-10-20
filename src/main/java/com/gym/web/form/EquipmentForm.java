package com.gym.web.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Equipment entry / update form (equipment-entry.php, edit-equipmentform.php). */
public class EquipmentForm {

    @NotBlank(message = "Equipment name is required")
    @Size(max = 30, message = "Name: max 30 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description: max 50 characters")
    private String description;

    @NotNull(message = "Date of purchase is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Vendor is required")
    @Size(max = 50, message = "Vendor: max 50 characters")
    private String vendor;

    @NotBlank(message = "Vendor address is required")
    @Size(max = 20, message = "Address: max 20 characters")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "\\d{6,15}", message = "Contact: digits only (6 to 15)")
    private String contact;

    /** Cost per item. Stored amount = cost x quantity (same as PHP). */
    @NotNull(message = "Cost per item is required")
    @Min(value = 0, message = "Cost cannot be negative")
    private Integer amount;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}
