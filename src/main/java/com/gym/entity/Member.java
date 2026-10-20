package com.gym.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "members")
public class Member {

    public static final String ACTIVE = "Active";
    public static final String EXPIRED = "Expired";
    public static final String PENDING = "Pending";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    private String fullname;
    private String username;
    private String password;
    private String gender;

    /** Date of registration. */
    private LocalDate dor;

    private String services;
    private int amount;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "p_year")
    private int pYear;

    /** Number of months ("0" means expired / none). */
    private String plan;

    private String address;
    private String contact;
    private String status = ACTIVE;

    @Column(name = "attendance_count")
    private int attendanceCount;

    @Column(name = "ini_weight")
    private int iniWeight;

    @Column(name = "curr_weight")
    private int currWeight;

    @Column(name = "ini_bodytype")
    private String iniBodytype = "";

    @Column(name = "curr_bodytype")
    private String currBodytype = "";

    @Column(name = "progress_date")
    private LocalDate progressDate;

    /** 1 = a payment reminder has been sent. */
    private int reminder;

    // ----- helpers used by the views -----

    @Transient
    public int getPlanMonths() {
        try {
            return Integer.parseInt(plan == null ? "0" : plan.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Transient
    public int getWeightDifference() {
        return currWeight - iniWeight;
    }

    /** Weight variation in percent, same formula as progress-percent.php. */
    @Transient
    public int getProgressPercent() {
        return iniWeight == 0 ? 0 : (int) ((currWeight - iniWeight) * 100.0 / iniWeight);
    }

    @Transient
    public boolean isReminderSent() {
        return reminder == 1;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
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
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
    public int getPYear() { return pYear; }
    public void setPYear(int pYear) { this.pYear = pYear; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttendanceCount() { return attendanceCount; }
    public void setAttendanceCount(int attendanceCount) { this.attendanceCount = attendanceCount; }
    public int getIniWeight() { return iniWeight; }
    public void setIniWeight(int iniWeight) { this.iniWeight = iniWeight; }
    public int getCurrWeight() { return currWeight; }
    public void setCurrWeight(int currWeight) { this.currWeight = currWeight; }
    public String getIniBodytype() { return iniBodytype; }
    public void setIniBodytype(String iniBodytype) { this.iniBodytype = iniBodytype; }
    public String getCurrBodytype() { return currBodytype; }
    public void setCurrBodytype(String currBodytype) { this.currBodytype = currBodytype; }
    public LocalDate getProgressDate() { return progressDate; }
    public void setProgressDate(LocalDate progressDate) { this.progressDate = progressDate; }
    public int getReminder() { return reminder; }
    public void setReminder(int reminder) { this.reminder = reminder; }
}
