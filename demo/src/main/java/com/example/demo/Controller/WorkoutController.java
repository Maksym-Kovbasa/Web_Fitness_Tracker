package com.example.demo.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Model.Workout;
import com.example.demo.Service.WorkoutServiceInterface;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutServiceInterface workoutService;

    public WorkoutController(WorkoutServiceInterface workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public ResponseEntity<?> getAllWorkouts(@RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String username = getCurrentUsername();
        List<Workout> userWorkouts = workoutService.getUserWorkouts(username, type, date);
        return ResponseEntity.ok(userWorkouts);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getWorkoutsByType(@PathVariable String type) {
        String username = getCurrentUsername();
        List<Workout> workoutsByType = workoutService.getUserWorkoutsByType(username, type);
        return ResponseEntity.ok(workoutsByType);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<?> getWorkoutsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String username = getCurrentUsername();
        List<Workout> workoutsByDate = workoutService.getUserWorkoutsByDate(username, date);
        return ResponseEntity.ok(workoutsByDate);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkoutById(@PathVariable Long id) {
        String username = getCurrentUsername();
        Optional<Workout> workout = workoutService.getWorkoutById(id, username);
        return ResponseEntity.ok(workout.get());
    }

    @PostMapping
    public ResponseEntity<?> createWorkout(@RequestBody Workout workout) {
        Workout savedWorkout = workoutService.createWorkout(workout);
        return ResponseEntity.ok(savedWorkout);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkout(@PathVariable Long id, @RequestBody Workout workout) {
        String username = getCurrentUsername();
        Workout updatedWorkout = workoutService.updateWorkout(id, workout, username);
        return ResponseEntity.ok(updatedWorkout);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Long id) {
        String username = getCurrentUsername();
        workoutService.deleteWorkout(id, username);
        return ResponseEntity.ok("Workout deleted successfully.");
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}