package com.gym.repository;

import com.gym.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Integer> {

    List<Todo> findByUserIdOrderById(Integer userId);

    Optional<Todo> findByIdAndUserId(Integer id, Integer userId);
}
