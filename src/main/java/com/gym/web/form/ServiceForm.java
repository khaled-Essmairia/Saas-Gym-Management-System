package com.gym.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Add / update form of a service (table "rates"). */
public class ServiceForm {

    @NotBlank(message = "Service name is required")
    @Size(max = 50, message = "Name: max 50 characters")
    private String name;

    @NotNull(message = "Monthly price is required")
    @Min(value = 1, message = "Monthly price must be at least 1")
    @Max(value = 100000, message = "Monthly price must be at most 100000")
    private Integer charge;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCharge() { return charge; }
    public void setCharge(Integer charge) { this.charge = charge; }
}
