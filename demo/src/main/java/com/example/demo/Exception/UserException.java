package com.example.demo.Exception;

/**
 * Represents a custom exception for handling user-related errors in the application.
 * This exception can be thrown to indicate various user-specific error conditions.
 */
public class UserException extends RuntimeException{
    
    public UserException(String message) {
        super(message);
    }

    public static UserException userNotFound(Long id) {
        return new UserException(String.format("User with id %d not found.", id));
    }

    public static UserException usernameAlreadyExists(String username) {
        return new UserException(String.format("Username '%s' already exists.", username));
    }

    public static UserException emailAlreadyExists(String email) {
        return new UserException(String.format("Email '%s' already exists.", email));
    }

    public static UserException invalidPassword() {
        return new UserException("Invalid password provided.");
    }

    public static UserException invalidUserId(Long id) {
        return new UserException(String.format("Invalid user ID: %d. It must be a positive number.", id));
    }

    public static UserException accessDenied(Long id) {
        return new UserException(String.format("Access denied for user with id %d.", id));
    }
    
    public static UserException userDataRequired() {
        return new UserException("User data is required");
    }

    public static UserException usernameRequired() {
        return new UserException("Username is required and cannot be empty");
    }

    public static UserException emailRequired() {
        return new UserException("Email is required and cannot be empty");
    }

    public static UserException invalidEmailFormat() {
        return new UserException("Invalid email format");
    }

    public static UserException passwordRequired() {
        return new UserException("Password is required and cannot be empty");
    }

    public static UserException passwordTooShort() {
        return new UserException("Password must be at least 6 characters long");
    }

    public static UserException currentUserNotFound() {
        return new UserException("Current user not found");
    }
}
