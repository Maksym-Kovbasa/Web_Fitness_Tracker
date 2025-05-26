package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Goal;
import com.example.demo.Model.User;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserUsername(String username);
    List<Goal> findByUser(User user);
}
