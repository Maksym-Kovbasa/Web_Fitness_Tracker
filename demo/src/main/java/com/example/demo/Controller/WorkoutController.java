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

    public WorkoutController(WorkoutRepository workoutRepository, UserRepository userRepository, GoalServiceInterface goalService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.goalService = goalService;
    }

    @GetMapping
    public List<Workout> getAllWorkouts(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (userId != null || type != null || date != null) {
            return workoutRepository.findAll().stream()
                    .filter(w -> userId == null || (w.getUser() != null && w.getUser().getId().equals(userId)))
                    .filter(w -> type == null || w.getType().equalsIgnoreCase(type))
                    .filter(w -> date == null || w.getDate().equals(date))
                    .toList();
        }
        return workoutRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkoutById(@PathVariable Long id) {
        return workoutRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createWorkout(@RequestBody Workout workout) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null)
            return ResponseEntity.status(401).body("User not found");
        workout.setUser(user);
        Workout saved = workoutRepository.save(workout);

        goalService.updateAllGoalsProgressForUserAndDate(user, workout.getDate());

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkout(@PathVariable Long id, @RequestBody Workout workout) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return workoutRepository.findById(id)
                .filter(w -> w.getUser() != null && w.getUser().getUsername().equals(username))
                .map(existing -> {
                    existing.setType(workout.getType());
                    existing.setDuration(workout.getDuration());
                    existing.setCalories(workout.getCalories());
                    existing.setDate(workout.getDate());
                    workoutRepository.save(existing);
                    return ResponseEntity.ok((Object) existing);
                })
                .orElse(ResponseEntity.status(403).body("Forbidden"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkout(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Workout workout = workoutOpt.get();
        if (workout.getUser() == null || !workout.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        workoutRepository.delete(workout);

        goalService.updateAllGoalsProgressForUserAndDate(workout.getUser(), workout.getDate());

        return ResponseEntity.noContent().build();
    }
}