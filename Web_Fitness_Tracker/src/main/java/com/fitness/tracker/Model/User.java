package com.fitness.tracker.Model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"goals", "workouts", "password"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "\"user\"")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler",})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private String role;
    private String password;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private Set<Goal> goals;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private Set<Workout> workouts;
}
