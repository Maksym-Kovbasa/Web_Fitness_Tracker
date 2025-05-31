package com.fitness.tracker.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitness.tracker.Model.User;
import com.fitness.tracker.Model.Workout;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserUsername(String username);
    List<Workout> findByUser(User user);
    List<Workout> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    List<Workout> findByUserUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);
    int countByUserUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);
}