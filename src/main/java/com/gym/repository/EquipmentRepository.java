package com.gym.repository;

import com.gym.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {

    @Query("select coalesce(sum(e.amount), 0) from Equipment e")
    long totalExpenses();
}
