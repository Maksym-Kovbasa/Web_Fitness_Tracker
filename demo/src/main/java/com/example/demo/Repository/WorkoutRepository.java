package com.example.demo.Repository;

import com.example.demo.Model.Workout;
import com.example.demo.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserUsername(String username);
    List<Workout> findByUser(User user);
    List<Workout> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    List<Workout> findByUserUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);
    int countByUserUsernameAndDateBetween(String username, LocalDate startDate, LocalDate endDate);
}