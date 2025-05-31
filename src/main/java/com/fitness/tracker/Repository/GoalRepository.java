package com.fitness.tracker.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.tracker.Model.Goal;
import com.fitness.tracker.Model.User;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserUsername(String username);
    List<Goal> findByUser(User user);
}
