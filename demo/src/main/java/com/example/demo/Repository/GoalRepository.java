package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Goal;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserUsername(String username);
}
