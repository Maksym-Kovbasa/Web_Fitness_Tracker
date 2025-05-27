package com.example.demo.Controller;

import com.example.demo.Model.User;
import com.example.demo.Model.Workout;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;


    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleDateTimeParseException(DateTimeParseException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Invalid date format");
        error.put("message", "Please use format YYYY-MM-DD and ensure the date is valid");
        error.put("details", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @GetMapping
    public ResponseEntity<?> getGeneralStats() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            List<Workout> userWorkouts = getUserWorkouts(currentUser);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalWorkouts", userWorkouts.size());
            stats.put("totalDuration", userWorkouts.stream().mapToInt(Workout::getDuration).sum());
            stats.put("totalCalories", userWorkouts.stream().mapToInt(Workout::getCalories).sum());
            stats.put("averageDuration",
                    userWorkouts.isEmpty() ? 0 : userWorkouts.stream().mapToInt(Workout::getDuration).average().orElse(0));
            stats.put("averageCalories",
                    userWorkouts.isEmpty() ? 0 : userWorkouts.stream().mapToInt(Workout::getCalories).average().orElse(0));

            String mostPopularType = userWorkouts.stream()
                    .collect(Collectors.groupingBy(Workout::getType, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("None");
            stats.put("mostPopularWorkoutType", mostPopularType);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @GetMapping("/workouts/by-type")
    public ResponseEntity<?> getWorkoutStatsByType() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            List<Workout> userWorkouts = getUserWorkouts(currentUser);

            Map<String, Map<String, Object>> statsByType = userWorkouts.stream()
                    .collect(Collectors.groupingBy(Workout::getType))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                List<Workout> workouts = entry.getValue();
                                Map<String, Object> typeStats = new HashMap<>();
                                typeStats.put("count", workouts.size());
                                typeStats.put("totalDuration", workouts.stream().mapToInt(Workout::getDuration).sum());
                                typeStats.put("totalCalories", workouts.stream().mapToInt(Workout::getCalories).sum());
                                typeStats.put("averageDuration",
                                        workouts.stream().mapToInt(Workout::getDuration).average().orElse(0));
                                typeStats.put("averageCalories",
                                        workouts.stream().mapToInt(Workout::getCalories).average().orElse(0));
                                return typeStats;
                            }));

            return ResponseEntity.ok(statsByType);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @GetMapping("/progress/calories")
    public ResponseEntity<?> getCaloriesProgress(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String period) {

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            LocalDate parsedEndDate = parseDate(endDate, LocalDate.now());
            LocalDate parsedStartDate = parseDate(startDate, parsedEndDate.minusMonths(1));

            if (parsedStartDate.isAfter(parsedEndDate)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid date range", "message", "Start date must be before or equal to end date"));
            }

            List<Workout> userWorkouts = getUserWorkouts(currentUser).stream()
                    .filter(w -> !w.getDate().isBefore(parsedStartDate) && !w.getDate().isAfter(parsedEndDate))
                    .collect(Collectors.toList());

            Map<String, Object> progressData = new HashMap<>();
            progressData.put("startDate", parsedStartDate);
            progressData.put("endDate", parsedEndDate);
            progressData.put("period", period);

            switch (period.toLowerCase()) {
                case "daily":
                    progressData.put("data", getDailyCaloriesProgress(userWorkouts, parsedStartDate, parsedEndDate));
                    break;
                case "weekly":
                    progressData.put("data", getWeeklyCaloriesProgress(userWorkouts, parsedStartDate, parsedEndDate));
                    break;
                case "monthly":
                    progressData.put("data", getMonthlyCaloriesProgress(userWorkouts, parsedStartDate, parsedEndDate));
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid period", "message", "Use: daily, weekly, or monthly"));
            }

            return ResponseEntity.ok(progressData);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid date format", "message", "Please use format YYYY-MM-DD and ensure the date is valid", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    @GetMapping("/workouts/by-period")
    public ResponseEntity<?> getWorkoutsByPeriod(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            LocalDate parsedEndDate = parseDate(endDate, LocalDate.now());
            LocalDate parsedStartDate = parseDate(startDate, parsedEndDate.minusMonths(1));

            if (parsedStartDate.isAfter(parsedEndDate)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid date range", "message", "Start date must be before or equal to end date"));
            }

            List<Workout> userWorkouts = getUserWorkouts(currentUser).stream()
                    .filter(w -> !w.getDate().isBefore(parsedStartDate) && !w.getDate().isAfter(parsedEndDate))
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("startDate", parsedStartDate);
            stats.put("endDate", parsedEndDate);
            stats.put("totalWorkouts", userWorkouts.size());
            stats.put("totalDuration", userWorkouts.stream().mapToInt(Workout::getDuration).sum());
            stats.put("totalCalories", userWorkouts.stream().mapToInt(Workout::getCalories).sum());
            stats.put("workoutsByType", userWorkouts.stream()
                    .collect(Collectors.groupingBy(Workout::getType, Collectors.counting())));

            return ResponseEntity.ok(stats);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid date format", "message", "Please use format YYYY-MM-DD and ensure the date is valid", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }

    private LocalDate parseDate(String dateString, LocalDate defaultValue) throws DateTimeParseException {
        if (dateString == null || dateString.trim().isEmpty()) {
            return defaultValue;
        }
        return LocalDate.parse(dateString.trim());
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private List<Workout> getUserWorkouts(User user) {
        return workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getDailyCaloriesProgress(List<Workout> workouts, LocalDate startDate,
            LocalDate endDate) {
        Map<LocalDate, Integer> dailyCalories = workouts.stream()
                .collect(Collectors.groupingBy(
                        Workout::getDate,
                        Collectors.summingInt(Workout::getCalories)));

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            final LocalDate currentDate = current;
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currentDate);
            dayData.put("calories", dailyCalories.getOrDefault(currentDate, 0));
            dayData.put("workouts", workouts.stream()
                    .filter(w -> w.getDate().equals(currentDate))
                    .count());
            result.add(dayData);
            current = current.plusDays(1);
        }
        return result;
    }

    private List<Map<String, Object>> getWeeklyCaloriesProgress(List<Workout> workouts, LocalDate startDate,
            LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            LocalDate weekEnd = current.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }

            final LocalDate finalWeekEnd = weekEnd;
            final LocalDate weekStart = current;

            List<Workout> weekWorkouts = workouts.stream()
                    .filter(w -> !w.getDate().isBefore(weekStart) && !w.getDate().isAfter(finalWeekEnd))
                    .collect(Collectors.toList());

            Map<String, Object> weekData = new HashMap<>();
            weekData.put("weekStart", weekStart);
            weekData.put("weekEnd", finalWeekEnd);
            weekData.put("calories", weekWorkouts.stream().mapToInt(Workout::getCalories).sum());
            weekData.put("workouts", weekWorkouts.size());
            result.add(weekData);

            current = current.plusWeeks(1);
        }
        return result;
    }

    private List<Map<String, Object>> getMonthlyCaloriesProgress(List<Workout> workouts, LocalDate startDate,
            LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate current = startDate.withDayOfMonth(1);

        while (!current.isAfter(endDate)) {
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            final LocalDate finalMonthEnd = monthEnd;
            final LocalDate monthStart = current;

            List<Workout> monthWorkouts = workouts.stream()
                    .filter(w -> !w.getDate().isBefore(monthStart) && !w.getDate().isAfter(finalMonthEnd))
                    .collect(Collectors.toList());

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", current.getYear() + "-" + String.format("%02d", current.getMonthValue()));
            monthData.put("calories", monthWorkouts.stream().mapToInt(Workout::getCalories).sum());
            monthData.put("workouts", monthWorkouts.size());
            result.add(monthData);

            current = current.plusMonths(1);
        }
        return result;
    }
}