package com.gym.entity;

import jakarta.persistence.*;

/**
 * Check-in record. Date and time are stored as text, exactly like the PHP
 * version ("2022-06-02" and "09:15 AM").
 */
@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "curr_date")
    private String currDate;

    @Column(name = "curr_time")
    private String currTime;

    private boolean present;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCurrDate() { return currDate; }
    public void setCurrDate(String currDate) { this.currDate = currDate; }
    public String getCurrTime() { return currTime; }
    public void setCurrTime(String currTime) { this.currTime = currTime; }
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
}
