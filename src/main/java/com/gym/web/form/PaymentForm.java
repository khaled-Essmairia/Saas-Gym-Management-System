package com.gym.web.form;

import jakarta.validation.constraints.*;

/** Payment form (user-payment.php). */
public class PaymentForm {

    /** Amount per month. */
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private Integer amount;

    /** Months: 1, 3, 6, 12 or 0 (none / expired). */
    @NotNull
    private Integer plan = 1;

    @NotBlank
    private String status = "Active";

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public Integer getPlan() { return plan; }
    public void setPlan(Integer plan) { this.plan = plan; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
