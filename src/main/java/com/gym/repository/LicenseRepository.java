package com.gym.repository;

import com.gym.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Integer> {

    /** The active license = the most recently entered one. */
    Optional<License> findFirstByOrderByIdDesc();

    List<License> findTop10ByOrderByIdDesc();
}
