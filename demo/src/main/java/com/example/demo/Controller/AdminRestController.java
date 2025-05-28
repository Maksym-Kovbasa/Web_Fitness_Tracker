package com.example.demo.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Model.Goal;
import com.example.demo.Model.User;
import com.example.demo.Model.Workout;
import com.example.demo.Repository.GoalRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WorkoutRepository;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== Users ==========

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                return ResponseEntity.ok(userOpt.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            Optional<User> existingOpt = userRepository.findById(id);
            if (existingOpt.isPresent()) {
                User existing = existingOpt.get();
                existing.setUsername(user.getUsername());
                existing.setEmail(user.getEmail());
                existing.setRole(user.getRole());
                
                if (user.getPassword() != null && !user.getPassword().isBlank()) {
                    existing.setPassword(passwordEncoder.encode(user.getPassword()));
                }
                
                User savedUser = userRepository.save(existing);
                return ResponseEntity.ok(savedUser);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            User userToDelete = userOpt.get();

            String currentUsername = authentication.getName();
            if (userToDelete.getUsername().equals(currentUsername)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "You cannot delete your own account");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            List<Goal> userGoals = goalRepository.findByUser(userToDelete);
            goalRepository.deleteAll(userGoals);

            List<Workout> userWorkouts = workoutRepository.findByUser(userToDelete);
            workoutRepository.deleteAll(userWorkouts);

            userRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User successfully deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error during delete the user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/users/{id}/goals")
    public ResponseEntity<Map<String, String>> deleteUserGoals(@PathVariable Long id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();
            List<Goal> userGoals = goalRepository.findByUser(user);
            
            if (userGoals.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "The user has no goals to delete.");
                return ResponseEntity.ok(response);
            }

            goalRepository.deleteAll(userGoals);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Deleted " + userGoals.size() + " user goals");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error while deleting targets: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== Goals ==========

    @GetMapping("/goals")
    public ResponseEntity<List<Goal>> getAllGoals() {
        try {
            List<Goal> goals = goalRepository.findAll();
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/goals/{id}")
    public ResponseEntity<Goal> getGoal(@PathVariable Long id) {
        try {
            Optional<Goal> goalOpt = goalRepository.findById(id);
            if (goalOpt.isPresent()) {
                return ResponseEntity.ok(goalOpt.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/goals/{id}")
    public ResponseEntity<Goal> updateGoal(@PathVariable Long id, @RequestBody Goal goal) {
        try {
            Optional<Goal> existingOpt = goalRepository.findById(id);
            if (existingOpt.isPresent()) {
                Goal existing = existingOpt.get();
                existing.setTargetCalories(goal.getTargetCalories());
                existing.setTargetWorkouts(goal.getTargetWorkouts());
                existing.setStartDate(goal.getStartDate());
                existing.setEndDate(goal.getEndDate());
                
                Goal savedGoal = goalRepository.save(existing);
                return ResponseEntity.ok(savedGoal);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        try {
            Optional<Goal> goalOpt = goalRepository.findById(id);
            if (goalOpt.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Goal not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            goalRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Goal successfully deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error during deleting the goal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== Workouts ==========

    @GetMapping("/workouts")
    public ResponseEntity<List<Workout>> getAllWorkouts() {
        try {
            List<Workout> workouts = workoutRepository.findAll();
            return ResponseEntity.ok(workouts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/workouts/{id}")
    public ResponseEntity<Workout> getWorkout(@PathVariable Long id) {
        try {
            Optional<Workout> workoutOpt = workoutRepository.findById(id);
            if (workoutOpt.isPresent()) {
                return ResponseEntity.ok(workoutOpt.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/workouts")
    public ResponseEntity<Workout> createWorkout(@RequestBody Workout workout) {
        try {
            Workout savedWorkout = workoutRepository.save(workout);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedWorkout);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/workouts/{id}")
    public ResponseEntity<Workout> updateWorkout(@PathVariable Long id, @RequestBody Workout workout) {
        try {
            Optional<Workout> existingOpt = workoutRepository.findById(id);
            if (existingOpt.isPresent()) {
                Workout existing = existingOpt.get();
                existing.setType(workout.getType());
                existing.setDuration(workout.getDuration());
                existing.setCalories(workout.getCalories());
                existing.setDate(workout.getDate());
                
                Workout savedWorkout = workoutRepository.save(existing);
                return ResponseEntity.ok(savedWorkout);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<Map<String, String>> deleteWorkout(@PathVariable Long id) {
        try {
            Optional<Workout> workoutOpt = workoutRepository.findById(id);
            if (workoutOpt.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Workout not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            workoutRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Workout successfully deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error during deleting the workout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== Statistics ==========

    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            long totalUsers = userRepository.count();
            long totalGoals = goalRepository.count();
            long totalWorkouts = workoutRepository.count();
            
            stats.put("totalUsers", totalUsers);
            stats.put("totalGoals", totalGoals);
            stats.put("totalWorkouts", totalWorkouts);

            List<User> allUsers = userRepository.findAll();
            long adminCount = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long userCount = allUsers.stream().filter(u -> "USER".equals(u.getRole())).count();
            
            stats.put("adminCount", adminCount);
            stats.put("userCount", userCount);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/users")
    public ResponseEntity<Map<String, Object>> getUsersStatistics() {
        try {
            List<User> users = userRepository.findAll();
            Map<String, Object> stats = new HashMap<>();
            
            for (User user : users) {
                Map<String, Object> userStats = new HashMap<>();
                
                List<Goal> userGoals = goalRepository.findByUser(user);
                List<Workout> userWorkouts = workoutRepository.findByUser(user);
                
                userStats.put("goalsCount", userGoals.size());
                userStats.put("workoutsCount", userWorkouts.size());
                userStats.put("totalCalories", userWorkouts.stream().mapToInt(Workout::getCalories).sum());
                userStats.put("totalDuration", userWorkouts.stream().mapToInt(Workout::getDuration).sum());
                
                stats.put(user.getUsername(), userStats);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}