package com.example.demo.Exception;

/**
 * Custom exception class for handling goal-related errors in the application.
 * Provides static factory methods for creating specific goal-related exceptions
 * with descriptive error messages.
 */
public class GoalException extends RuntimeException {

    public GoalException(String message) {
        super(message);
    }

    public static GoalException goalNotFound(Long id) {
        return new GoalException(String.format("Goal with id %d not found.", id));
    }

    public static GoalException goalHaveNotYet(String username) {
        return new GoalException("The goals have not been set yet.");
    }

    public static GoalException goalAccessDenied(Long id) {
        return new GoalException(String.format("Access denied: Goal with id %d does not belong to you.", id));
    }

    public static GoalException invalidGoalData(String field) {
        return new GoalException(String.format("Invalid goal data: %s is required.", field));
    }

    public static GoalException goalDateConflict() {
        return new GoalException("Goal end date cannot be before start date.");
    }

    public static GoalException userNotFound(String username) {
        return new GoalException(String.format("User %s not found.", username));
    }

    public static GoalException goalInvalidFilter() {
        return new GoalException("Status parameter is required and cannot be empty. Make sure you are using one of the following statuses:In%20Progress, Completed, Not%20Started, Failed");
    }
}
