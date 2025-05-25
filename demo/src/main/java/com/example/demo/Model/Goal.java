package com.example.demo.Model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int goalNumber;
    private int targetCalories;
    private int targetWorkouts;
    private LocalDate startDate;
    private LocalDate endDate;
    private String username;
    private String status;

    @Column(name = "workout_progress")
    private int workoutProgress;

    public Goal(Long id, int targetCalories, int targetWorkouts, LocalDate startDate, LocalDate endDate, User user) {
        this.id = getId();
        this.targetCalories = getTargetCalories();
        this.targetWorkouts = getTargetWorkouts();
        this.startDate = getStartDate();
        this.endDate = getEndDate();
        this.username = getUser() != null ? getUser().getUsername() : null;
    }

    public int getGoalNumber() {
        return goalNumber;
    }

    public void setGoalNumber(int goalNumber) {
        this.goalNumber = goalNumber;
    }

    public int getWorkoutProgress() {
        return workoutProgress;
    }

    public void setWorkoutProgress(int workoutProgress) {
        this.workoutProgress = workoutProgress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}