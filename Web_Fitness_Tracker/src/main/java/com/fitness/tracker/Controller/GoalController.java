package com.fitness.tracker.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.tracker.Exception.GoalException;
import com.fitness.tracker.Model.Goal;
import com.fitness.tracker.Model.User;
import com.fitness.tracker.Repository.GoalRepository;
import com.fitness.tracker.Repository.UserRepository;
import com.fitness.tracker.Service.GoalServiceInterface;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalServiceInterface goalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    public GoalController(GoalServiceInterface goalService) {
        this.goalService = goalService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoal(@PathVariable Long id) {
        Goal goal = goalRepository.findById(id).orElseThrow(() -> GoalException.goalNotFound(id));
        String username = getAuthentication();
        if (!goal.getUsername().equals(username)) {
            throw GoalException.goalAccessDenied(id);
        }
        return ResponseEntity.ok(goal);
    }

    @GetMapping
    public ResponseEntity<List<Goal>> getAllGoals() {
        String username = getAuthentication();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> GoalException.userNotFound(username));

        List<Goal> goals = goalRepository.findByUserUsername(username);
        if(goals.isEmpty()) { throw GoalException.goalHaveNotYet(); }
        return ResponseEntity.ok(goals);
    }

    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody Goal goal) {
        validateGoalData(goal);
        Goal savedGoal = goalService.createGoal(goal);
        return ResponseEntity.ok(savedGoal);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @RequestBody Goal goal) {
        validateGoalData(goal);
        return goalService.updateGoal(id, goal)
                .map(updated -> {
                    return ResponseEntity.ok(updated);
                })
                .orElseThrow(() -> {
                    return GoalException.goalNotFound(id);
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id) {
        if (!goalService.deleteGoal(id)) {
            throw GoalException.goalNotFound(id);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")  
    public ResponseEntity<List<Goal>> getGoalsByStatus(@RequestParam("status") String status) {
        String username = getAuthentication();
        List<Goal> goals = goalRepository.findByUserUsername(username);
        validateStatus(status);
        // (Completed, In Progress, Not Started, Failed)
        List<Goal> filteredGoals = goals.stream()
                .filter(goal -> goal.getStatus() != null && goal.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filteredGoals);
    }

    private String getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private void validateGoalData(Goal goal) {
        if (goal.getTargetCalories() == null || goal.getTargetCalories() <= 0) { throw GoalException.invalidGoalData("targetCalories"); }
        if (goal.getTargetWorkouts() == null || goal.getTargetWorkouts() <= 0) { throw GoalException.invalidGoalData("targetWorkouts"); }
        if (goal.getStartDate() == null) { throw GoalException.invalidGoalData("startDate"); }
        if (goal.getEndDate() == null) { throw GoalException.invalidGoalData("endDate"); }
        if (goal.getEndDate().isBefore(goal.getStartDate())) { throw GoalException.goalDateConflict(); }
    }

    private void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) { throw GoalException.goalInvalidFilter(); }
    
        List<String> validStatuses = List.of("In Progress", "Not Started", "Completed", "Failed");
    
        if (validStatuses.stream().noneMatch(validStatus -> validStatus.equalsIgnoreCase(status.trim()))) {
            throw new IllegalArgumentException( String.format("Invalid status '%s'. Valid statuses are: %s", status, String.join(", ", validStatuses)));
        }
    }
}