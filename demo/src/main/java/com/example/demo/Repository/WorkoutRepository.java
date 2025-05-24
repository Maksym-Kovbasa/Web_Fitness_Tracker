package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Workout;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
}