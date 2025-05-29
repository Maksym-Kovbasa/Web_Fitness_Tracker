package com.example.demo.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        validateWorkoutData(workout);
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserException.currentUserNotFound());
        workout.setUser(user);
        Workout saved = workoutRepository.save(workout);
        goalService.updateAllGoalsProgressForUserAndDate(user, workout.getDate());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkout(@PathVariable Long id, @RequestBody Workout workout) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.currentUserNotFound());

        Optional<Workout> existingWorkoutOpt = workoutRepository.findById(id);
        if (existingWorkoutOpt.isEmpty()) {
            throw WorkoutException.workoutNotFound(id);
        }
        validateWorkoutData(workout);
        Workout existingWorkout = existingWorkoutOpt.get();

        if (existingWorkout.getUser() == null || !existingWorkout.getUser().getId().equals(currentUser.getId())) {
            throw WorkoutException.workoutAccessDenied(id);
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
        workoutRepository.delete(workout);
        goalService.updateAllGoalsProgressForUserAndDate(currentUser, workout.getDate());
        return ResponseEntity.ok("Workout deleted successfully.");
    }

    private void validateWorkoutData(Workout workout) {
        if (workout.getType() == null || workout.getType().isEmpty()) {
            throw WorkoutException.invalidWorkoutData("type");
        }
        if (workout.getDuration() == null || workout.getDuration() <= 0) {
            throw WorkoutException.invalidWorkoutData("duration");
        }
        if (workout.getCalories() == null || workout.getCalories() <= 0) {
            throw WorkoutException.invalidWorkoutData("calories");
        }
        if (workout.getDate() == null) {
            throw WorkoutException.invalidWorkoutData("date");
        }
    }
}