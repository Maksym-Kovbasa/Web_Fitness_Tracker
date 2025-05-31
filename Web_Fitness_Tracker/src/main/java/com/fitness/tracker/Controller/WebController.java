package com.fitness.tracker.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fitness.tracker.Model.Goal;
import com.fitness.tracker.Model.User;
import com.fitness.tracker.Model.Workout;
import com.fitness.tracker.Repository.GoalRepository;
import com.fitness.tracker.Repository.UserRepository;
import com.fitness.tracker.Repository.WorkoutRepository;
import com.fitness.tracker.Service.GoalServiceInterface;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalServiceInterface goalService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GoalRepository goalRepository;

    @GetMapping("/")
    public String homePage() {

        return "about";
    }

    @GetMapping("/home")
    public String homePageAlias() {
        return "about";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/register";
            }
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Email already exists");
                return "redirect:/register";
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("USER");
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        return "redirect:/?logout=true";
    }

    @GetMapping("/web/workouts")
    public String workoutsPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<Workout> workouts = workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getUsername().equals(username))
                .toList();

        model.addAttribute("workouts", workouts);
        model.addAttribute("username", username);
        model.addAttribute("newWorkout", new Workout());
        return "workouts";
    }

    @PostMapping("/web/workouts")
    public String createWorkout(@ModelAttribute("newWorkout") Workout workout, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/web/workouts";
            }
            workout.setUser(user);
            workoutRepository.save(workout);
            goalService.updateAllGoalsProgressForUserAndDate(user, workout.getDate());
            redirectAttributes.addFlashAttribute("success", "Тренування успішно додано!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при додаванні тренування: " + e.getMessage());
        }

        return "redirect:/web/workouts";
    }

    @PostMapping("/web/workouts/{id}/delete")
    public String deleteWorkout(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Workout workout = workoutRepository.findById(id).orElse(null);
            if (workout == null) {
                redirectAttributes.addFlashAttribute("error", "Тренування не знайдено");
                return "redirect:/web/workouts";
            }
            if (workout.getUser() == null || !workout.getUser().getUsername().equals(username)) {
                redirectAttributes.addFlashAttribute("error", "Доступ заборонено");
                return "redirect:/web/workouts";
            }
            workoutRepository.delete(workout);
            goalService.updateAllGoalsProgressForUserAndDate(workout.getUser(), workout.getDate());
            redirectAttributes.addFlashAttribute("success", "Тренування успішно видалено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при видаленні тренування: " + e.getMessage());
        }

        return "redirect:/web/workouts";
    }

    @GetMapping("/web/goals")
    public String goalsPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/login";
        }
        List<Goal> goals = goalService.getGoalsByUser(user);
        model.addAttribute("goals", goals);
        model.addAttribute("username", username);
        model.addAttribute("newGoal", new Goal());
        return "goals";
    }

    @PostMapping("/web/goals")
    public String createGoal(@ModelAttribute("newGoal") Goal goal, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/web/goals";
            }
            goal.setUser(user);
            goalService.createGoal(goal);
            redirectAttributes.addFlashAttribute("success", "Goal created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating goal: " + e.getMessage());
        }

        return "redirect:/web/goals";
    }

    @PostMapping("/web/goals/{id}/delete")
    public String deleteGoal(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/web/goals";
            }
            Optional<Goal> goalOptional = goalRepository.findById(id);
            if (goalOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Goal not found");
                return "redirect:/web/goals";
            }
            Goal goal = goalOptional.get();
            if (goal.getUser() == null || !goal.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Access denied: Goal does not belong to you");
                return "redirect:/web/goals";
            }
            goalService.deleteGoal(id);
            redirectAttributes.addFlashAttribute("success", "Goal deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting goal: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/web/goals";
    }

    @GetMapping("/web/stats")
    public String statsPage(Model model,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        LocalDate finalEndDate = (endDate == null || endDate.isEmpty()) ? LocalDate.now() : LocalDate.parse(endDate);
        LocalDate finalStartDate = (startDate == null || startDate.isEmpty()) ? finalEndDate.minusMonths(1)
                : LocalDate.parse(startDate);

        List<Workout> userWorkouts = getUserWorkouts(currentUser);
        List<Workout> periodWorkouts = userWorkouts.stream()
                .filter(w -> !w.getDate().isBefore(finalStartDate) && !w.getDate().isAfter(finalEndDate))
                .collect(Collectors.toList());

        Map<String, Object> generalStats = calculateGeneralStats(userWorkouts);
        Map<String, Object> periodStats = calculatePeriodStats(periodWorkouts, finalStartDate, finalEndDate);
        Map<String, Map<String, Object>> statsByType = calculateStatsByType(userWorkouts);

        List<Map<String, Object>> dailyProgress = getDailyProgress(periodWorkouts, finalStartDate, finalEndDate);
        Map<String, Long> workoutsByType = getWorkoutsByType(userWorkouts);

        model.addAttribute("user", currentUser);
        model.addAttribute("generalStats", generalStats);
        model.addAttribute("periodStats", periodStats);
        model.addAttribute("statsByType", statsByType);
        model.addAttribute("dailyProgress", dailyProgress);
        model.addAttribute("workoutsByType", workoutsByType);
        model.addAttribute("startDate", finalStartDate);
        model.addAttribute("endDate", finalEndDate);

        return "stats";
    }

    @GetMapping("/web/stats/export")
    @ResponseBody
    public String exportStats(@RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        userRepository.findByUsername(username).orElse(null);
        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            return "error,User not authenticated";
        }

        LocalDate finalEndDate = (endDate == null || endDate.isEmpty()) ? LocalDate.now() : LocalDate.parse(endDate);
        LocalDate finalStartDate = (startDate == null || startDate.isEmpty()) ? finalEndDate.minusMonths(1)
                : LocalDate.parse(startDate);

        List<Workout> userWorkouts = getUserWorkouts(currentUser);
        List<Workout> periodWorkouts = userWorkouts.stream()
                .filter(w -> !w.getDate().isBefore(finalStartDate) && !w.getDate().isAfter(finalEndDate))
                .collect(Collectors.toList());

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Type,Duration,Calories,Description\n");

        for (Workout workout : periodWorkouts) {
            csv.append(workout.getDate()).append(",")
                    .append(workout.getType()).append(",")
                    .append(workout.getDuration()).append(",")
                    .append(workout.getCalories()).append(",")
                    .append("\n");
        }

        return csv.toString();
    }

    @GetMapping("/web/index")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/web/goals/filter")
    public String filterGoals(@RequestParam(required = false) String status, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            model.addAttribute("error", "User not found");
            return "redirect:/login";
        }

        List<Goal> goals = goalRepository.findByUserUsername(username);

        if (status != null && !status.isEmpty()) {
            goals = goals.stream()
                    .filter(goal -> goal.getStatus() != null && goal.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        model.addAttribute("goals", goals);
        model.addAttribute("username", username);
        model.addAttribute("newGoal", new Goal());
        return "goals";
    }

    private List<Workout> getUserWorkouts(User user) {
        return workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }

    private Map<String, Object> calculateGeneralStats(List<Workout> workouts) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalWorkouts", workouts.size());
        stats.put("totalDuration", workouts.stream().mapToInt(Workout::getDuration).sum());
        stats.put("totalCalories", workouts.stream().mapToInt(Workout::getCalories).sum());

        double avgDuration = workouts.isEmpty() ? 0
                : workouts.stream().mapToInt(Workout::getDuration).average().orElse(0);
        stats.put("averageDuration", avgDuration);

        double avgCalories = workouts.isEmpty() ? 0
                : workouts.stream().mapToInt(Workout::getCalories).average().orElse(0);
        stats.put("averageCalories", avgCalories);

        String mostPopularType = workouts.stream()
                .collect(Collectors.groupingBy(Workout::getType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        stats.put("mostPopularType", mostPopularType);

        return stats;
    }

    private Map<String, Object> calculatePeriodStats(List<Workout> workouts, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalWorkouts", workouts.size());
        stats.put("totalDuration", workouts.stream().mapToInt(Workout::getDuration).sum());
        stats.put("totalCalories", workouts.stream().mapToInt(Workout::getCalories).sum());

        double avgDuration = workouts.isEmpty() ? 0
                : workouts.stream().mapToInt(Workout::getDuration).average().orElse(0);
        stats.put("averageDuration", avgDuration);

        double avgCalories = workouts.isEmpty() ? 0
                : workouts.stream().mapToInt(Workout::getCalories).average().orElse(0);
        stats.put("averageCalories", avgCalories);

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double avgWorkoutsPerDay = daysBetween > 0 ? (double) workouts.size() / daysBetween : 0;
        stats.put("averageWorkoutsPerDay", avgWorkoutsPerDay);

        return stats;
    }

    private Map<String, Map<String, Object>> calculateStatsByType(List<Workout> workouts) {
        return workouts.stream()
                .collect(Collectors.groupingBy(Workout::getType))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            List<Workout> typeWorkouts = entry.getValue();
                            Map<String, Object> typeStats = new HashMap<>();
                            typeStats.put("count", typeWorkouts.size());
                            typeStats.put("totalDuration", typeWorkouts.stream().mapToInt(Workout::getDuration).sum());
                            typeStats.put("totalCalories", typeWorkouts.stream().mapToInt(Workout::getCalories).sum());
                            typeStats.put("averageDuration",
                                    typeWorkouts.stream().mapToInt(Workout::getDuration).average().orElse(0));
                            typeStats.put("averageCalories",
                                    typeWorkouts.stream().mapToInt(Workout::getCalories).average().orElse(0));
                            return typeStats;
                        }));
    }

    private List<Map<String, Object>> getDailyProgress(List<Workout> workouts, LocalDate startDate, LocalDate endDate) {

        Map<LocalDate, List<Workout>> workoutsByDate = workouts.stream()
                .collect(Collectors.groupingBy(Workout::getDate));

        List<Map<String, Object>> dailyProgress = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            Map<String, Object> dayData = new HashMap<>();
            List<Workout> dayWorkouts = workoutsByDate.getOrDefault(current, Collections.emptyList());

            dayData.put("date", current.toString());
            dayData.put("calories", dayWorkouts.stream().mapToInt(Workout::getCalories).sum());
            dayData.put("workouts", dayWorkouts.size());
            dayData.put("duration", dayWorkouts.stream().mapToInt(Workout::getDuration).sum());

            dailyProgress.add(dayData);
            current = current.plusDays(1);
        }

        return dailyProgress;
    }

    private Map<String, Long> getWorkoutsByType(List<Workout> workouts) {
        return workouts.stream()
                .collect(Collectors.groupingBy(Workout::getType, Collectors.counting()));
    }
}
