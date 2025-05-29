package com.example.demo.Model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "goals", "workouts", "password", "role", "id"})
    private User user;

    private Integer targetCalories;
    private Integer targetWorkouts;
    private LocalDate startDate;
    private LocalDate endDate;
    private String username;
    private String status;

    @Column(name = "workout_progress")
    private Double workoutProgress;

    public Goal(Long id, int targetCalories, int targetWorkouts, LocalDate startDate, LocalDate endDate, User user) {
        this.id = id;
        this.targetCalories = targetCalories;
        this.targetWorkouts = targetWorkouts;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
        this.username = user != null ? user.getUsername() : null;
    }

    public Goal() {}

    public void setWorkoutProgress(double workoutProgress) {
        this.workoutProgress = workoutProgress;
    }

    public void setUser(User user) {
        this.user = user;
        this.username = user != null ? user.getUsername() : null;
    }
}