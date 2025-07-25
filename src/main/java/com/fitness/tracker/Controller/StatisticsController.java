package com.fitness.tracker.Controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.tracker.Exception.StatisticsException;
import com.fitness.tracker.Exception.UserException;
import com.fitness.tracker.Exception.WorkoutException;
import com.fitness.tracker.Model.User;
import com.fitness.tracker.Model.Workout;
import com.fitness.tracker.Repository.UserRepository;
import com.fitness.tracker.Repository.WorkoutRepository;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getGeneralStats() {
        try {
            User currentUser = getCurrentUser();
            List<Workout> userWorkouts = getUserWorkouts(currentUser);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalWorkouts", userWorkouts.size());
            stats.put("totalDuration", userWorkouts.stream().mapToInt(Workout::getDuration).sum());
            stats.put("totalCalories", userWorkouts.stream().mapToInt(Workout::getCalories).sum());
            stats.put("averageDuration", userWorkouts.isEmpty() ? 0
                    : userWorkouts.stream().mapToInt(Workout::getDuration).average().orElse(0));
            stats.put("averageCalories", userWorkouts.isEmpty() ? 0
                    : userWorkouts.stream().mapToInt(Workout::getCalories).average().orElse(0));

            String mostPopularType = userWorkouts.stream()
                    .collect(Collectors.groupingBy(Workout::getType, Collectors.counting()))
                    .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
                    .orElse("None");
            stats.put("mostPopularWorkoutType", mostPopularType);

            return ResponseEntity.ok(stats);
        } catch (Exception ex) {
            throw StatisticsException.feiledGeneralstats(ex.getMessage());
        }
    }

    @GetMapping("/workouts/by-type")
    public ResponseEntity<?> getWorkoutStatsByType() {
        User currentUser = getCurrentUser();
        List<Workout> userWorkouts = getUserWorkouts(currentUser);
        if (userWorkouts.isEmpty()) {
            throw WorkoutException.noWorkoutsFound();
        }
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
    }

    @GetMapping("/progress/calories")
    public ResponseEntity<?> getCaloriesProgress(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "daily") String period) {

        User currentUser = getCurrentUser();
        LocalDate parsedEndDate = parseDate(endDate, LocalDate.now());
        LocalDate parsedStartDate = parseDate(startDate, parsedEndDate.minusMonths(1));


        if (parsedEndDate.isBefore(parsedStartDate)) { throw StatisticsException.statisticsDateConflict(); }

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
                throw StatisticsException.invalidStatisticsData("Use: daily, weekly, or monthly");
        }
        return ResponseEntity.ok(progressData);
    }

    @GetMapping("/workouts/by-period")
    public ResponseEntity<?> getWorkoutsByPeriod(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
            User currentUser = getCurrentUser();

            LocalDate parsedEndDate = parseDate(endDate, LocalDate.now());
            LocalDate parsedStartDate = parseDate(startDate, parsedEndDate.minusMonths(1));
            if (parsedEndDate.isBefore(parsedStartDate)) { throw StatisticsException.statisticsDateConflict(); }

            List<Workout> userWorkouts = getUserWorkouts(currentUser).stream()
                    .filter(w -> !w.getDate().isBefore(parsedStartDate) && !w.getDate().isAfter(parsedEndDate))
                    .collect(Collectors.toList());

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("startDate", parsedStartDate);
            stats.put("endDate", parsedEndDate);
            stats.put("totalWorkouts", userWorkouts.size());
            stats.put("totalDuration", userWorkouts.stream().mapToInt(Workout::getDuration).sum());
            stats.put("totalCalories", userWorkouts.stream().mapToInt(Workout::getCalories).sum());
            stats.put("workoutsByType", userWorkouts.stream()
                    .collect(Collectors.groupingBy(Workout::getType, Collectors.counting())));

            return ResponseEntity.ok(stats);
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
        return userRepository.findByUsername(username).orElseThrow(() -> UserException.currentUserNotFound());
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