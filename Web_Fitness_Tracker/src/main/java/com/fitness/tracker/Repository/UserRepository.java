package com.fitness.tracker.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitness.tracker.Model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
