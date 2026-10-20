package com.gym.repository;

import com.gym.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    List<Attendance> findByCurrDate(String currDate);

    boolean existsByUserIdAndCurrDate(String userId, String currDate);

    long countByCurrDate(String currDate);

    @Transactional
    long deleteByUserIdAndCurrDate(String userId, String currDate);

    @Transactional
    long deleteByUserId(String userId);
}
