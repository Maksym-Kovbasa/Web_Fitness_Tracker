package com.example.demo.Exception;

public class StatisticsException extends RuntimeException {
    
    public StatisticsException(String message) {
        super(message);
    }

    /**
     * Creates a StatisticsException indicating the inability to find the statistics.
     *
     * @param id The identifier of the statistics the user is trying to find
     * @return Statistics with id not found.
     */
    public static StatisticsException statisticsNotFound(Long id) {
        return new StatisticsException(String.format("Statistics with id %d not found.", id));
    }

    /**
     * Creates a StatisticsException indicating unauthorized access to a specific statistics.
     *
     * @param id The ID of the statistics that the user does not have permission to access
     * @return Access denied to statistics with id.
     */
    public static StatisticsException statisticsAccessDenied(Long id) {
        return new StatisticsException(String.format("Access denied to statistics with id %d.", id));
    }

    /**
     * Creates a StatisticsException indicating that the statistics data is invalid.
     *
     * @param field The field that is required and missing or invalid
     * @return Invalid statistics data: field is required.
     */
    public static StatisticsException invalidStatisticsData(String field) {
        return new StatisticsException(String.format("Invalid statistics data: %s is required.", field));
    }

    /**
     * Creates a StatisticsException indicating a general failure in statistics processing.
     * @param message A message describing the failure
     * @return StatisticsException with the provided message.
     */
    public static StatisticsException feiledGeneralstats(String message) {
        return new StatisticsException("Failed to fetch general statistics: " + message);
    }


    /**
     * Creates a StatisticsException indicating a conflict in the date range for statistics.
     *
     * @return StatisticsException indicating that the start date must be before or equal to the end date.
     */
    public static StatisticsException statisticsDateConflict() {
        return new StatisticsException("Invalid date range. Start date must be before or equal to end date");
    }
    
}
