package com.example.demo.Exception;

import org.springframework.http.HttpStatus;

/**
 * Custom exception class for handling goal-related errors in the application.
 * Provides static factory methods for creating specific goal-related exceptions
 * with descriptive error messages.
 */
public class GoalException extends RuntimeException {
    private final HttpStatus status;

    public GoalException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

     public HttpStatus getStatus() {
        return status;
    }

    /**
     * Creates a GoalException indicating the inability to find the goal.
     *
     * @param id The identifier of the goal the user is trying to find
     * @return Goal with id not found.
     */
    public static GoalException goalNotFound(Long id) {
        return new GoalException(String.format("Goal with id %d not found.", id), HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a GoalException indicating to access a goals that have not been set yet.
     * @return The goals have not been set yet.
     */
    public static GoalException goalHaveNotYet() {
        return new GoalException("The goals have not been set yet.", HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a GoalException indicating unauthorized access to a specific goal.
     *
     * @param id The ID of the goal that the user does not have permission to access
     * @return Access denied: Goal with id does not belong to you.
     */
    public static GoalException goalAccessDenied(Long id) {
        return new GoalException(String.format("Access denied: Goal with id %d does not belong to you.", id), HttpStatus.FORBIDDEN);
    }

    /**
     * Creates a GoalException indicating that the goal data provided is invalid.
     *
     * @param field The field that is missing or invalid
     * @return Invalid goal data: field is required.
     */
    public static GoalException invalidGoalData(String field) {
        return new GoalException(String.format("Invalid goal data: %s is required.", field), HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a GoalException indicating that the goal end date cannot be before the start date.
     *
     * @return Goal end date cannot be before start date.
     */
    public static GoalException goalDateConflict() {
        return new GoalException("Goal end date cannot be before start date.", HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a GoalException indicating that the user associated with the goal was not found.
     *
     * @param username The username of the user that was not found
     * @return User with username not found.
     */
    public static GoalException userNotFound(String username) {
        return new GoalException(String.format("User %s not found.", username), HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a GoalException indicating that the status parameter is invalid or missing.
     *
     * @return Status parameter is required and cannot be empty.
     */
    public static GoalException goalInvalidFilter() {
        return new GoalException("Status parameter is required and cannot be empty. Make sure you are using one of the following statuses:In%20Progress, Completed, Not%20Started, Failed", HttpStatus.BAD_REQUEST);
    }
}