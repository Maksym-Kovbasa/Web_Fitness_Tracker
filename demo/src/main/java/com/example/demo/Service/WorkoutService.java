package com.example.demo.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Exception.UserException;
import com.example.demo.Exception.WorkoutException;
import com.example.demo.Model.User;
import com.example.demo.Model.Workout;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Repository.WorkoutRepository;

@Service
public class WorkoutService implements WorkoutServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(WorkoutService.class);

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final GoalServiceInterface goalService;

    public WorkoutService(WorkoutRepository workoutRepository,
            UserRepository userRepository,
            GoalServiceInterface goalService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.goalService = goalService;
    }

    @Override
    public List<Workout> getUserWorkouts(String username, String type, LocalDate date) {
        logger.debug("Fetching workouts for user: {}, type: {}, date: {}", username, type, date);

        User currentUser = getCurrentUser(username);

        return workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(currentUser.getId()))
                .filter(w -> type == null || w.getType().equalsIgnoreCase(type))
                .filter(w -> date == null || w.getDate().equals(date))
                .toList();
    }

    @Override
    public List<Workout> getUserWorkoutsByType(String username, String type) {
        logger.debug("Fetching workouts by type: {} for user: {}", type, username);

        User currentUser = getCurrentUser(username);

        List<Workout> workoutsByType = workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(currentUser.getId()))
                .filter(w -> w.getType().equalsIgnoreCase(type))
                .toList();

        if (workoutsByType.isEmpty()) {
            throw WorkoutException.workoutNotFoundByType(type);
        }

        return workoutsByType;
    }

    @Override
    public List<Workout> getUserWorkoutsByDate(String username, LocalDate date) {
        logger.debug("Fetching workouts by date: {} for user: {}", date, username);

        User currentUser = getCurrentUser(username);

        List<Workout> workoutsByDate = workoutRepository.findAll().stream()
                .filter(w -> w.getUser() != null && w.getUser().getId().equals(currentUser.getId()))
                .filter(w -> w.getDate().equals(date))
                .toList();

        if (workoutsByDate.isEmpty()) {
            throw WorkoutException.workoutNotFoundByDate(date);
        }

        return workoutsByDate;
    }

    @Override
    public Optional<Workout> getWorkoutById(Long id, String username) {
        logger.debug("Fetching workout with id: {} for user: {}", id, username);

        User currentUser = getCurrentUser(username);

        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            throw WorkoutException.workoutNotFound(id);
        }

        Workout workout = workoutOpt.get();
        if (workout.getUser() == null || !workout.getUser().getId().equals(currentUser.getId())) {
            throw WorkoutException.workoutAccessDenied(id);
        }

        return Optional.of(workout);
    }

    @Override
    @Transactional
    public Workout createWorkout(Workout workout) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        logger.debug("Creating workout for user: {}, time wor workout: {}", username, workout.getDate());
        validateWorkoutData(workout);

        User user = getCurrentUser(username);
        workout.setUser(user);

        Workout savedWorkout = workoutRepository.save(workout);

        //goalService.updateAllGoalsProgressForUserAndDate(user, workout.getDate());

        logger.debug("Successfully created workout with id: {}", savedWorkout.getId());
        return savedWorkout;
    }

    @Override
    @Transactional
    public Workout updateWorkout(Long id, Workout workout, String username) {
        logger.debug("Updating workout with id: {} for user: {}", id, username);

        validateWorkoutData(workout);

        User currentUser = getCurrentUser(username);

        Optional<Workout> existingWorkoutOpt = workoutRepository.findById(id);
        if (existingWorkoutOpt.isEmpty()) {
            throw WorkoutException.workoutNotFound(id);
        }

        Workout existingWorkout = existingWorkoutOpt.get();

        if (existingWorkout.getUser() == null || !existingWorkout.getUser().getId().equals(currentUser.getId())) {
            throw WorkoutException.workoutAccessDenied(id);
        }

        existingWorkout.setType(workout.getType());
        existingWorkout.setDuration(workout.getDuration());
        existingWorkout.setCalories(workout.getCalories());
        existingWorkout.setDate(workout.getDate());

        Workout updatedWorkout = workoutRepository.save(existingWorkout);

        logger.debug("Successfully updated workout with id: {}", id);
        return updatedWorkout;
    }

    @Override
    @Transactional
    public void deleteWorkout(Long id, String username) {
        logger.debug("Deleting workout with id: {} for user: {}", id, username);

        User currentUser = getCurrentUser(username);

        Optional<Workout> workoutOpt = workoutRepository.findById(id);
        if (workoutOpt.isEmpty()) {
            throw WorkoutException.workoutNotFound(id);
        }

        Workout workout = workoutOpt.get();
        if (workout.getUser() == null || !workout.getUser().getId().equals(currentUser.getId())) {
            throw WorkoutException.workoutAccessDenied(id);
        }

        LocalDate workoutDate = workout.getDate();

        workoutRepository.delete(workout);

        //goalService.updateAllGoalsProgressForUserAndDate(currentUser, workoutDate);

        logger.debug("Successfully deleted workout with id: {}", id);
    }

    @Override
    public void validateWorkoutData(Workout workout) {
        if (workout.getType() == null || workout.getType().trim().isEmpty()) {
            throw WorkoutException.invalidWorkoutData("type");
        }
        if (workout.getDuration() == null || workout.getDuration() <= 0) {
            throw WorkoutException.invalidWorkoutData("duration");
        }
        if (workout.getCalories() == null || workout.getCalories() <= 0) {
            throw WorkoutException.invalidWorkoutData("calories");
        }
        if (workout.getDate() == null) {
            throw WorkoutException.invalidWorkoutData("date");
        }
    }

    private User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserException.currentUserNotFound());
    }
}