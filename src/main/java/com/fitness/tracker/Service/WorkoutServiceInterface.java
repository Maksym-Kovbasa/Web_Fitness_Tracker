package com.fitness.tracker.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fitness.tracker.Model.Workout;

public interface WorkoutServiceInterface {
    List<Workout> getUserWorkouts(String username, String type, LocalDate date);
    List<Workout> getUserWorkoutsByType(String username, String type);
    List<Workout> getUserWorkoutsByDate(String username, LocalDate date);
    Optional<Workout> getWorkoutById(Long id, String username);
    Workout createWorkout(Workout workout);
    Workout updateWorkout(Long id, Workout workout, String username);
    void deleteWorkout(Long id, String username);
    void validateWorkoutData(Workout workout);
}