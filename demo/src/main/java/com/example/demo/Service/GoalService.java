package com.example.demo.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.Model.Goal;
import com.example.demo.Model.User;
import com.example.demo.Model.Workout;
import com.example.demo.Repository.GoalRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WorkoutRepository;

@Service
public class GoalService implements GoalServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(GoalService.class);
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final WorkoutRepository workoutRepository;

    public GoalService(GoalRepository goalRepository,
            UserRepository userRepository,
            WorkoutRepository workoutRepository) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.workoutRepository = workoutRepository;
    }

    public List<Goal> getCurrentUserGoals() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return goalRepository.findByUserUsername(username);
    }

    @Override
    public Goal createGoal(Goal goal) {
        if (goal.getTargetCalories() <= 0) {
            throw new IllegalArgumentException("Target calories must be greater than 0");
        }
        if (goal.getTargetWorkouts() <= 0) {
            throw new IllegalArgumentException("Target workouts must be greater than 0");
        }
        if (goal.getStartDate() == null || goal.getEndDate() == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        if (goal.getEndDate().isBefore(goal.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        goal.setUser(user);
        Goal savedGoal = goalRepository.save(goal);

        return updateGoalProgress(savedGoal.getId());
    }

    public int calculateTotalCalories(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return 0;
        }

        String username = auth.getName();
        List<Workout> workouts = workoutRepository.findByUserUsernameAndDateBetween(
                username, startDate, endDate);

        return workouts.stream()
                .mapToInt(Workout::getCalories)
                .sum();
    }

    public int calculateTotalWorkouts(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return 0;
        }

        String username = auth.getName();
        return workoutRepository.countByUserUsernameAndDateBetween(username, startDate, endDate);
    }

    public String getGoalStatus(Goal goal) {
        LocalDate now = LocalDate.now();
        if (now.isBefore(goal.getStartDate())) {
            return "Not Started";
        } else if (now.isAfter(goal.getEndDate())) {
            return goal.getWorkoutProgress() == 100 ? "Achieved!" : "Failed";
        } else {
            return "In Progress";
        }
    }

    public Goal updateGoalProgress(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        int totalWorkouts = calculateTotalWorkouts(goal.getStartDate(), goal.getEndDate());
        int totalCalories = calculateTotalCalories(goal.getStartDate(), goal.getEndDate());

        int workoutPercent = goal.getTargetWorkouts() > 0
                ? (totalWorkouts * 100) / goal.getTargetWorkouts()
                : 0;
        int caloriesPercent = goal.getTargetCalories() > 0
                ? (totalCalories * 100) / goal.getTargetCalories()
                : 0;

        int progress = Math.min(workoutPercent, caloriesPercent);
        progress = Math.min(progress, 100);

        goal.setWorkoutProgress(progress);
        // Обчислення статусу цілі:
        // Якщо поточна дата до старту – "Not Started",
        // Якщо після кінця – "Achieved!" (якщо прогрес 100%) або "Failed",
        // Інакше – "In Progress"
        goal.setStatus(getGoalStatus(goal));
        return goalRepository.save(goal);
    }

    public boolean isGoalAchieved(Goal goal) {
        int totalCalories = calculateTotalCalories(goal.getStartDate(), goal.getEndDate());
        int workoutCount = calculateTotalWorkouts(goal.getStartDate(), goal.getEndDate());

        boolean caloriesAchieved = totalCalories >= goal.getTargetCalories();
        boolean workoutsAchieved = workoutCount >= goal.getTargetWorkouts();

        return caloriesAchieved && workoutsAchieved &&
                goal.getTargetCalories() > 0 &&
                goal.getTargetWorkouts() > 0;
    }

    public Optional<Goal> getGoalById(Long id) {
        logger.debug("Attempting to fetch goal with id: {}", id);

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                logger.error("No authentication found");
                return Optional.empty();
            }

            String username = auth.getName();
            logger.debug("Fetching goal for user: {}", username);

            Optional<Goal> goalOpt = goalRepository.findById(id);
            if (goalOpt.isEmpty()) {
                logger.debug("Goal not found with id: {}", id);
                return Optional.empty();
            }

            Goal goal = goalOpt.get();
            updateGoalProgress(goal.getId());

            if (goal.getUser() == null) {
                logger.debug("Goal has no user assigned, attempting to assign current user");
                try {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("User not found: " + username));
                    goal.setUser(user);
                    goal = goalRepository.save(goal);
                    logger.debug("Successfully assigned user to goal");
                } catch (Exception e) {
                    logger.error("Failed to assign user to goal", e);
                    return Optional.empty();
                }
            }

            if (!goal.getUser().getUsername().equals(username)) {
                logger.debug("Goal {} belongs to different user", id);
                return Optional.empty();
            }

            logger.debug("Successfully retrieved goal {}", id);
            return Optional.of(goal);

        } catch (Exception e) {
            logger.error("Unexpected error fetching goal with id: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Goal> updateGoal(Long id, Goal updatedGoal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return goalRepository.findById(id)
                .filter(goal -> goal.getUser().getUsername().equals(username))
                .map(goal -> {
                    goal.setTargetCalories(updatedGoal.getTargetCalories());
                    goal.setTargetWorkouts(updatedGoal.getTargetWorkouts());
                    goal.setStartDate(updatedGoal.getStartDate());
                    goal.setEndDate(updatedGoal.getEndDate());
                    Goal updated = goalRepository.save(goal);
                    return updateGoalProgress(updated.getId());
                });
    }

    public boolean deleteGoal(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return goalRepository.findById(id)
                .filter(goal -> goal.getUser().getUsername().equals(username))
                .map(goal -> {
                    goalRepository.delete(goal);
                    return true;
                })
                .orElse(false);
    }

    public int getProgressPercentage(Goal goal) {
        if (goal.getTargetCalories() <= 0)
            return 0;

        int currentCalories = calculateTotalCalories(goal.getStartDate(), goal.getEndDate());
        int percentage = (currentCalories * 100) / goal.getTargetCalories();

        return Math.min(percentage, 100);
    }

    public void updateAllGoalsProgressForUserAndDate(User user, LocalDate workoutDate) {
        List<Goal> goals = goalRepository.findByUserUsername(user.getUsername());
        for (Goal goal : goals) {
            if ((workoutDate.isEqual(goal.getStartDate()) || workoutDate.isAfter(goal.getStartDate()))
                    && (workoutDate.isEqual(goal.getEndDate()) || workoutDate.isBefore(goal.getEndDate()))) {
                updateGoalProgress(goal.getId());
            }
        }
    }
}