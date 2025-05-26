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

import com.example.demo.Model.User;
import com.example.demo.Model.Workout;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WorkoutRepository;
import com.example.demo.Service.GoalServiceInterface;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final GoalServiceInterface goalService;

    public WorkoutController(WorkoutRepository workoutRepository, UserRepository userRepository,
            GoalServiceInterface goalService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.goalService = goalService;
    }

    @GetMapping
    public ResponseEntity<?> getAllWorkouts(@RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        List<Workout> userWorkouts = workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(currentUser.getId()))
                .filter(w -> type == null || w.getType().equalsIgnoreCase(type))
                .filter(w -> date == null || w.getDate().equals(date))
                .toList();

        return ResponseEntity.ok(userWorkouts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkoutById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Workout workout = workoutOpt.get();
        if (workout.getUser() == null || !workout.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Workout does not belong to you");
        }
        return ResponseEntity.ok(workout);
    }

    @PostMapping
    public ResponseEntity<?> createWorkout(@RequestBody Workout workout) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        workout.setUser(user);
        Workout saved = workoutRepository.save(workout);
        goalService.updateAllGoalsProgressForUserAndDate(user, workout.getDate());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkout(@PathVariable Long id, @RequestBody Workout workout) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Optional<Workout> existingWorkoutOpt = workoutRepository.findById(id);
        if (existingWorkoutOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Workout existingWorkout = existingWorkoutOpt.get();

        if (existingWorkout.getUser() == null || !existingWorkout.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Workout does not belong to you");
        }
        existingWorkout.setType(workout.getType());
        existingWorkout.setDuration(workout.getDuration());
        existingWorkout.setCalories(workout.getCalories());
        existingWorkout.setDate(workout.getDate());
        Workout updated = workoutRepository.save(existingWorkout);
        goalService.updateAllGoalsProgressForUserAndDate(currentUser, updated.getDate());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Workout workout = workoutOpt.get();
        if (workout.getUser() == null || !workout.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Workout does not belong to you");
        }
        workoutRepository.delete(workout);
        goalService.updateAllGoalsProgressForUserAndDate(currentUser, workout.getDate());
        return ResponseEntity.noContent().build();
    }
}