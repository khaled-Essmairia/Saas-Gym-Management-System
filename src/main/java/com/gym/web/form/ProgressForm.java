package com.gym.web.form;

import jakarta.validation.constraints.*;

/** Customer progress form (update-progress.php). */
public class ProgressForm {

    @NotNull(message = "Initial weight is required")
    @Min(value = 0, message = "Weight cannot be negative")
    private Integer iniWeight;

    @NotNull(message = "Current weight is required")
    @Min(value = 0, message = "Weight cannot be negative")
    private Integer currWeight;

    @Size(max = 50, message = "Body type: max 50 characters")
    private String iniBodytype;

    @Size(max = 50, message = "Body type: max 50 characters")
    private String currBodytype;

    public Integer getIniWeight() { return iniWeight; }
    public void setIniWeight(Integer iniWeight) { this.iniWeight = iniWeight; }
    public Integer getCurrWeight() { return currWeight; }
    public void setCurrWeight(Integer currWeight) { this.currWeight = currWeight; }
    public String getIniBodytype() { return iniBodytype; }
    public void setIniBodytype(String iniBodytype) { this.iniBodytype = iniBodytype; }
    public String getCurrBodytype() { return currBodytype; }
    public void setCurrBodytype(String currBodytype) { this.currBodytype = currBodytype; }
}
