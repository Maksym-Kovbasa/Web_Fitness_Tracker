package com.example.demo.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Exception.UserException;
import com.example.demo.Exception.WorkoutException;
import com.example.demo.Model.User;
import com.example.demo.Model.Workout;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WorkoutRepository;
import com.example.demo.Service.GoalServiceInterface;
import com.example.demo.Service.WorkoutServiceInterface;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final WorkoutServiceInterface workoutService;

    public WorkoutController(WorkoutRepository workoutRepository, UserRepository userRepository,
            WorkoutServiceInterface workoutService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.workoutService = workoutService;
    }

    @GetMapping
    public ResponseEntity<?> getAllWorkouts(@RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.currentUserNotFound());
        List<Workout> userWorkouts = workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(currentUser.getId()))
                .filter(w -> type == null || w.getType().equalsIgnoreCase(type))
                .filter(w -> date == null || w.getDate().equals(date))
                .toList();

        return ResponseEntity.ok(userWorkouts);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getWorkoutsByType(@PathVariable String type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.currentUserNotFound());
        List<Workout> workoutsByType = workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(currentUser.getId()))
                .filter(w -> w.getType().equalsIgnoreCase(type))
                .toList();

        if (workoutsByType.isEmpty()) {
            throw WorkoutException.workoutNotFoundByType(type);
        }
        return ResponseEntity.ok(workoutsByType);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<?> getWorkoutsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.currentUserNotFound());
        List<Workout> workoutsByDate = workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(currentUser.getId()))
                .filter(w -> w.getDate().equals(date))
                .toList();

        if (workoutsByDate.isEmpty()) {
            throw WorkoutException.workoutNotFoundByDate(date);
        }
        return ResponseEntity.ok(workoutsByDate);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkoutById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.currentUserNotFound());
        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            throw WorkoutException.workoutNotFound(id);
        }
        Workout workout = workoutOpt.get();
        if (workout.getUser() == null || !workout.getUser().getId().equals(currentUser.getId())) {
            throw WorkoutException.workoutAccessDenied(id);
        }
        return ResponseEntity.ok(workout);
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
        User currentUser = getCurrentUser(username);
        
        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            throw WorkoutException.workoutNotFound(id);
        }
        
        Workout workout = workoutOpt.get();
        if (workout.getUser() == null || !workout.getUser().getId().equals(currentUser.getId())) {
            throw WorkoutException.workoutAccessDenied(id);
        }
        
        LocalDate workoutDate = workout.getDate();
        
        workoutRepository.delete(workout);
        return ResponseEntity.ok("Workout deleted successfully.");
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.currentUserNotFound());
    }
}