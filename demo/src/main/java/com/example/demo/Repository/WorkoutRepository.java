package com.example.demo.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.Model.Workout;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);
    int countByUserUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);
}