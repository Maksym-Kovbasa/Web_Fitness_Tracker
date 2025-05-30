package com.example.demo.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.validator.routines.EmailValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Exception.AdminException;
import com.example.demo.Exception.GoalException;
import com.example.demo.Exception.UserException;
import com.example.demo.Exception.WorkoutException;
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
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw AdminException.usersNotFound();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserException.userNotFound(id));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> UserException.userNotFound(id));

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setRole(user.getRole());
        existing.setPassword(passwordEncoder.encode(user.getPassword()));
        validate(user);
        User savedUser = userRepository.save(existing);
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id, Authentication authentication) {

        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> UserException.userNotFound(id));

        if (userToDelete.getUsername().equals(authentication.getName())) {
            throw AdminException.cannotDeleteOwnAccount();
        }
        goalRepository.deleteAll(goalRepository.findByUser(userToDelete));
        workoutRepository.deleteAll(workoutRepository.findByUser(userToDelete));
        userRepository.deleteById(id);

        return ResponseEntity.ok(Map.of("message", "User successfully deleted"));
    }

    @DeleteMapping("/users/{id}/goals")
    public ResponseEntity<Map<String, String>> deleteUserGoals(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserException.userNotFound(id));

        List<Goal> userGoals = goalRepository.findByUser(user);
        if (userGoals.isEmpty()) {
            throw AdminException.userHasNoGoals();
        }

        goalRepository.deleteAll(userGoals);
        return ResponseEntity.ok(Map.of("message", "Deleted " + userGoals.size() + " user goals"));
    }

    // ========== Goals ==========

    @GetMapping("/goals")
    public ResponseEntity<List<Goal>> getAllGoals() {
        List<Goal> goals = goalRepository.findAll();
        if (goals.isEmpty()) {
            throw AdminException.goalEmpty();
        }
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/goals/{id}")
    public ResponseEntity<Goal> getGoal(@PathVariable Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> GoalException.goalNotFound(id));
        return ResponseEntity.ok(goal);
    }

    @PutMapping("/goals/{id}")
    public ResponseEntity<Goal> updateGoal(@PathVariable Long id, @RequestBody Goal goal) {
        Goal existing = goalRepository.findById(id).orElseThrow(() -> GoalException.goalNotFound(id));
        validateGoalData(existing);
        existing.setTargetCalories(goal.getTargetCalories());
        existing.setTargetWorkouts(goal.getTargetWorkouts());
        existing.setStartDate(goal.getStartDate());
        existing.setEndDate(goal.getEndDate());
        Goal savedGoal = goalRepository.save(existing);
        return ResponseEntity.ok(savedGoal);
    }

    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        if (!goalRepository.existsById(id)) {
            throw GoalException.goalNotFound(id);
        }
        goalRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Goal successfully deleted");
        return ResponseEntity.ok(response);
    }

    // ========== Workouts ==========

    @GetMapping("/workouts")
    public ResponseEntity<List<Workout>> getAllWorkouts() {
        List<Workout> workouts = workoutRepository.findAll();
        if (workouts.isEmpty()) {
            throw AdminException.usersNotFound();
        }
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/workouts/{id}")
    public ResponseEntity<Workout> getWorkout(@PathVariable Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() -> WorkoutException.workoutNotFound(id));
        return ResponseEntity.ok(workout);

    }

    @PostMapping("/workouts")
    public ResponseEntity<Workout> createWorkout(@RequestBody Workout workout) {
        validateWorkoutData(workout);
        Workout savedWorkout = workoutRepository.save(workout);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWorkout);

    }

    @PutMapping("/workouts/{id}")
    public ResponseEntity<Workout> updateWorkout(@PathVariable Long id, @RequestBody Workout workout) {
        Workout existing = workoutRepository.findById(id)
                .orElseThrow(() -> WorkoutException.workoutNotFound(id));
        validateWorkoutData(workout);
        existing.setType(workout.getType());
        existing.setDuration(workout.getDuration());
        existing.setCalories(workout.getCalories());
        existing.setDate(workout.getDate());

        Workout savedWorkout = workoutRepository.save(existing);
        return ResponseEntity.ok(savedWorkout);
    }

    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<Map<String, String>> deleteWorkout(@PathVariable Long id) {
        if (!workoutRepository.existsById(id)) {
            throw WorkoutException.workoutNotFound(id);
        }
        workoutRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Workout successfully deleted");
        return ResponseEntity.ok(response);
    }

    // ========== Statistics ==========

    @GetMapping("/stats/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalGoals = goalRepository.count();
        long totalWorkouts = workoutRepository.count();

        stats.put("totalUsers", totalUsers);
        stats.put("totalGoals", totalGoals);
        stats.put("totalWorkouts", totalWorkouts);

        List<User> allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            throw AdminException.usersNotFound();
        }
        long adminCount = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long userCount = allUsers.stream().filter(u -> "USER".equals(u.getRole())).count();

        stats.put("adminCount", adminCount);
        stats.put("userCount", userCount);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/users")
    public ResponseEntity<Map<String, Object>> getUsersStatistics() {
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
    }

    private void validate(User user) {
        if (user == null) {
            throw UserException.userDataRequired();
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw UserException.usernameRequired();
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw UserException.emailRequired();
        }
        if (!isValidEmail(user.getEmail())) {
            throw UserException.invalidEmailFormat();
        }
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (user.getPassword().length() < 6) {
                throw UserException.passwordTooShort();
            }
        }
    }

    private boolean isValidEmail(String email) { return EmailValidator.getInstance().isValid(email); }

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

     private void validateGoalData(Goal goal) {
        if (goal.getTargetCalories() == null || goal.getTargetCalories() <= 0) { throw GoalException.invalidGoalData("targetCalories"); }
        if (goal.getTargetWorkouts() == null || goal.getTargetWorkouts() <= 0) { throw GoalException.invalidGoalData("targetWorkouts"); }
        if (goal.getStartDate() == null) { throw GoalException.invalidGoalData("startDate"); }
        if (goal.getEndDate() == null) { throw GoalException.invalidGoalData("endDate"); }
        if (goal.getEndDate().isBefore(goal.getStartDate())) { throw GoalException.goalDateConflict(); }
    }

}