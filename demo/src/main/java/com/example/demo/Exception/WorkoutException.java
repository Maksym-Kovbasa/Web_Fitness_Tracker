package com.example.demo.Exception;

import java.time.LocalDate;

/**
 * Custom exception class for handling workout-related errors in the
 * application.
 * Provides static factory methods for creating specific workout-related
 * exceptions
 * with descriptive error messages.
 */
public class WorkoutException extends RuntimeException {

    public WorkoutException(String message) {
        super(message);
    }

    

    /***
     * Creates a WorkoutException indicating the inability to find the
     * workout.
     * 
     * @param id The identifier of the workout the user is trying to
     *           find
     * @return Workout with id not found.
     */
    public static WorkoutException workoutNotFound(Long id) {
        return new WorkoutException(String.format("Workout with id %d not found.", id));
    }

    /**
     * Creates a WorkoutException indicating unauthorized access to a specific
     * workout.
     *
     * @param id The ID of the workout that the user does not have permission to
     *           access
     * @return Access denied: Workout with id does not belong to you.
     */
    public static WorkoutException workoutAccessDenied(Long id) {
        return new WorkoutException(String.format("Access denied: Workout with id %d does not belong to you.", id));
    }

    /**
     * Creates a WorkoutException indicating that the workout data is invalid.
     *
     * @param field The field that is required and missing or invalid
     * @return Invalid workout data: field is required.
     */
    public static WorkoutException invalidWorkoutData(String field) {
        return new WorkoutException(String.format("Invalid workout data: %s is required.", field));
    }

    /**
     * Creates a WorkoutException indicating that the workout end date cannot be
     * before the start date.
     *
     * @return Workout end date cannot be before start date.
     */
    public static WorkoutException workoutDateConflict() {
        return new WorkoutException("Workout end date cannot be before start date.");
    }

    /**
     * Creates a WorkoutException indicating that a required parameter is missing.
     *
     * @param param The name of the parameter that is required
     * @return Invalid parameter: param is required.
     */
    public static WorkoutException invalidParam(String param) {
        return new WorkoutException(String.format("Invalid parameter: %s is required.", param));
    }

    /**
     * Creates a WorkoutException indicating that no workouts were found for the
     * specified parameter.
     *
     * @param type The type for which no workouts were found
     * @return No workouts found with type.
     */
    public static WorkoutException workoutNotFoundByType(String type) {
        return new WorkoutException(String.format("No workouts found with type: '%s'.", type));
    }

    /**
     * Creates a WorkoutException indicating that no workouts were found on the
     * specified date.
     *
     * @param date The date for which no workouts were found
     * @return No workouts found on date.
     */
    public static WorkoutException workoutNotFoundByDate(LocalDate date) {
        return new WorkoutException(String.format("No workouts found on date: %s.", date));
    }

    /**
     * Creates a WorkoutException indicating that no workouts were found for the
     * current user.
     *
     * @return No workouts found for the current user.
     */
    public static WorkoutException noWorkoutsFound() {
        return new WorkoutException("Not found workouts for the current user.");

    }
}
