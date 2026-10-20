package com.gym.entity;

import jakarta.persistence.*;

/** Monthly price of a service (Fitness, Sauna, Cardio). */
@Entity
@Table(name = "rates")
public class Rate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String charge;

    @Transient
    public int getChargeValue() {
        try {
            return Integer.parseInt(charge.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCharge() { return charge; }
    public void setCharge(String charge) { this.charge = charge; }
}
