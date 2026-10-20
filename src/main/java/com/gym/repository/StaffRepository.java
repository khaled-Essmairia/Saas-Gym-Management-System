package com.gym.repository;

import com.gym.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findFirstByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, Integer userId);

    long countByDesignation(String designation);

    @Query("select s.designation, count(s) from Staff s group by s.designation order by s.designation")
    List<Object[]> countByDesignationGroup();
}
