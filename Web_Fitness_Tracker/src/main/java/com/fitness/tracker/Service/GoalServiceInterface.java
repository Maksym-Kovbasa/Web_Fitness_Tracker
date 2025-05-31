package com.fitness.tracker.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fitness.tracker.Model.Goal;
import com.fitness.tracker.Model.User;

public interface GoalServiceInterface {
    Optional<Goal> getGoalById(Long id);

    List<Goal> getCurrentUserGoals();

    Goal createGoal(Goal goal);

    Optional<Goal> updateGoal(Long id, Goal goal);

    boolean deleteGoal(Long id);

    void updateAllGoalsProgressForUserAndDate(User user, LocalDate workoutDate);

    String getGoalStatus(Goal goal);

    List<Goal> getGoalsByUser(User user);

    void fixInvalidProgressValues();
}