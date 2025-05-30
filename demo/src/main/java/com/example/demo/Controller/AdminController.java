package com.example.demo.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.Goal;
import com.example.demo.Model.User;
import com.example.demo.Model.Workout;
import com.example.demo.Repository.GoalRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WorkoutRepository;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/web/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String adminPanel(Model model, Authentication authentication) {
        if (authentication == null ||
                authentication.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/access-denied";
        }

        List<User> users = userRepository.findAll();
        List<Goal> goals = goalRepository.findAll();
        List<Workout> workouts = workoutRepository.findAll();

        model.addAttribute("users", users);
        model.addAttribute("goals", goals);
        model.addAttribute("workouts", workouts);
        return "adminpanel";
    }

    // ----------- Керування користувачами -----------

    // Сторінка редагування користувача
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/web/admin?userNotFound";
        }
        model.addAttribute("user", userOpt.get());
        return "admin-user-edit";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
            @ModelAttribute User user,
            @RequestParam(value = "password", required = false) String password,
            RedirectAttributes redirectAttributes) {
        Optional<User> existingOpt = userRepository.findById(id);
        if (existingOpt.isPresent()) {
            User existing = existingOpt.get();
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setRole(user.getRole());
            if (password != null && !password.isBlank()) {
                existing.setPassword(passwordEncoder.encode(password));
            }
            userRepository.save(existing);
            redirectAttributes.addFlashAttribute("success", "Користувача оновлено.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Користувача не знайдено.");
        }
        return "redirect:/web/admin";
    }

    // Видалення користувача
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes,
            Authentication authentication) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Користувача не знайдено.");
                return "redirect:/web/admin";
            }

            User userToDelete = userOpt.get();

            // Перевіряємо, чи не намагається адмін видалити самого себе
            String currentUsername = authentication.getName();
            if (userToDelete.getUsername().equals(currentUsername)) {
                redirectAttributes.addFlashAttribute("error", "Ви не можете видалити самого себе.");
                return "redirect:/web/admin";
            }

            // Спочатку видаляємо всі пов'язані цілі користувача
            List<Goal> userGoals = goalRepository.findByUser(userToDelete);
            goalRepository.deleteAll(userGoals);

            // Потім видаляємо всі пов'язані тренування користувача
            List<Workout> userWorkouts = workoutRepository.findByUser(userToDelete);
            workoutRepository.deleteAll(userWorkouts);

            // Нарешті видаляємо самого користувача
            userRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("success",
                    "Користувача '" + userToDelete.getUsername() + "' успішно видалено разом з усіма його даними.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при видаленні користувача: " + e.getMessage());
        }
        return "redirect:/web/admin";
    }

    // ----------- Керування цілями -----------

    // Сторінка редагування цілі
    @GetMapping("/goals/edit/{id}")
    public String editGoalForm(@PathVariable Long id, Model model) {
        Optional<Goal> goalOpt = goalRepository.findById(id);
        if (goalOpt.isEmpty()) {
            return "redirect:/web/admin?goalNotFound";
        }
        model.addAttribute("goal", goalOpt.get());
        return "admin-goal-edit";
    }

    // Збереження зміненої цілі
    @PostMapping("/goals/edit/{id}")
    public String updateGoal(@PathVariable Long id, @ModelAttribute Goal goal,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Goal> existingOpt = goalRepository.findById(id);
            if (existingOpt.isPresent()) {
                Goal existing = existingOpt.get();
                if (goal.getTargetCalories() <= 0) {
                    redirectAttributes.addFlashAttribute("error", "Цільові калорії повинні бути більше 0");
                    return "redirect:/web/admin/goals/edit/" + id;
                }
                if (goal.getTargetWorkouts() <= 0) {
                    redirectAttributes.addFlashAttribute("error", "Цільові тренування повинні бути більше 0");
                    return "redirect:/web/admin/goals/edit/" + id;
                }
                if (goal.getEndDate().isBefore(goal.getStartDate())) {
                    redirectAttributes.addFlashAttribute("error", "Дата завершення не може бути раніше дати початку");
                    return "redirect:/web/admin/goals/edit/" + id;
                }
                existing.setTargetCalories(goal.getTargetCalories());
                existing.setTargetWorkouts(goal.getTargetWorkouts());
                existing.setStartDate(goal.getStartDate());
                existing.setEndDate(goal.getEndDate());
                goalRepository.save(existing);
                redirectAttributes.addFlashAttribute("success",
                        "Ціль користувача '" + existing.getUser().getUsername() + "' успішно оновлено.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Ціль не знайдено.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при оновленні цілі: " + e.getMessage());
        }
        return "redirect:/web/admin";
    }

    // Видалення цілі
    @PostMapping("/goals/{id}/delete")
    public String deleteGoal(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {
            Optional<Goal> goalOpt = goalRepository.findById(id);
            if (goalOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Ціль не знайдено.");
                return "redirect:/web/admin";
            }

            Goal goalToDelete = goalOpt.get();
            String goalOwner = goalToDelete.getUser() != null ? goalToDelete.getUser().getUsername() : "невідомий";

            goalRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success",
                    "Ціль користувача '" + goalOwner + "' успішно видалено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при видаленні цілі: " + e.getMessage());
        }
        return "redirect:/web/admin";
    }

    // Видалення всіх цілей конкретного користувача
    @PostMapping("/users/{userId}/goals/delete-all")
    public String deleteAllUserGoals(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Користувача не знайдено.");
                return "redirect:/web/admin";
            }

            User user = userOpt.get();
            List<Goal> userGoals = goalRepository.findByUser(user);

            if (userGoals.isEmpty()) {
                redirectAttributes.addFlashAttribute("info",
                        "У користувача '" + user.getUsername() + "' немає цілей для видалення.");
            } else {
                goalRepository.deleteAll(userGoals);
                redirectAttributes.addFlashAttribute("success",
                        "Видалено " + userGoals.size() + " цілей користувача '" + user.getUsername() + "'.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при видаленні цілей: " + e.getMessage());
        }
        return "redirect:/web/admin";
    }

    // // Сторінка перегляду цілей конкретного користувача
    // @GetMapping("/users/{userId}/goals")
    // public String viewUserGoals(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
    //     Optional<User> userOpt = userRepository.findById(userId);
    //     if (userOpt.isEmpty()) {
    //         redirectAttributes.addFlashAttribute("error", "Користувача не знайдено.");
    //         return "redirect:/web/admin";
    //     }

    //     User user = userOpt.get();
    //     List<Goal> userGoals = goalRepository.findByUser(user);

    //     model.addAttribute("user", user);
    //     model.addAttribute("goals", userGoals);
    //     return "admin-user-goals";
    // }

    // ----------- Керування тренуваннями -----------

    // Сторінка редагування тренування
    @GetMapping("/workouts/edit/{id}")
    public String editWorkoutForm(@PathVariable Long id, Model model) {
        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            return "redirect:/web/admin?workoutNotFound";
        }
        model.addAttribute("workout", workoutOpt.get());
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin-workout-edit";
    }

    // Збереження зміненого тренування
    @PostMapping("/workouts/edit/{id}")
    public String updateWorkout(@PathVariable Long id, @ModelAttribute Workout workout,
            @RequestParam(required = false) Long userId,
            RedirectAttributes redirectAttributes) {
        Optional<Workout> existingOpt = workoutRepository.findById(id);
        if (existingOpt.isPresent()) {
            Workout existing = existingOpt.get();
            existing.setType(workout.getType());
            existing.setDuration(workout.getDuration());
            existing.setCalories(workout.getCalories());
            existing.setDate(workout.getDate());
            
            if (userId != null) {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    existing.setUser(userOpt.get());
                }
            }

            workoutRepository.save(existing);
            redirectAttributes.addFlashAttribute("success", "Тренування оновлено.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Тренування не знайдено.");
        }
        return "redirect:/web/admin";
    }

    // Видалення тренування
    @PostMapping("/workouts/{id}/delete")
    public String deleteWorkout(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Workout> workoutOpt = workoutRepository.findById(id);
            if (workoutOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Тренування не знайдено.");
                return "redirect:/web/admin";
            }

            Workout workoutToDelete = workoutOpt.get();
            String workoutOwner = workoutToDelete.getUser() != null ? workoutToDelete.getUser().getUsername()
                    : "невідомий";

            workoutRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success",
                    "Тренування користувача '" + workoutOwner + "' успішно видалено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка при видаленні тренування: " + e.getMessage());
        }
        return "redirect:/web/admin";
    }

}