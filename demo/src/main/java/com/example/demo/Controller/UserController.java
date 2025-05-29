package com.example.demo.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Exception.UserException;
import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        validate(user);
        if (userRepository.findByUsername(user.getUsername()).isPresent()) { throw UserException.usernameAlreadyExists(user.getUsername()); }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) { throw UserException.emailAlreadyExists(user.getEmail()); }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        if (id == null || id <= 0) { throw UserException.invalidUserId(id); }
        User user = userRepository.findById(id).orElseThrow(() -> UserException.userNotFound(id));
        if (!user.getId().equals(id)) {
        throw UserException.accessDenied(id);
    }
        return ResponseEntity.ok(user);
    }


    // @GetMapping("/{id}")
    // public ResponseEntity<?> getUser(@PathVariable Long id) {
    //     if (id == null || id <= 0) { 
    //         throw UserException.invalidUserId(id); 
    //     }
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     String username = auth.getName();
    //     User currentUser = userRepository.findByUsername(username).orElse(null);
    //     if (currentUser == null) {
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
    //     }
    //     if (!currentUser.getId().equals(id)) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only access your own profile");
    //     }
    //     return ResponseEntity.ok(currentUser);
    // }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> UserException.currentUserNotFound());
        return ResponseEntity.ok(currentUser);
    }


    private void validate(User user) {
        if (user == null) {                                                      throw UserException.userDataRequired(); }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) { throw UserException.usernameRequired(); }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {       throw UserException.emailRequired(); }
        if (!isValidEmail(user.getEmail())) {                                    throw UserException.invalidEmailFormat(); }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) { throw UserException.passwordRequired(); }
        if (user.getPassword().length() < 6) {                                   throw UserException.passwordTooShort(); }
    }

    private boolean isValidEmail(String email) { return email.contains("@gmail.com") /*&& email.length() > 5*/;}
}
