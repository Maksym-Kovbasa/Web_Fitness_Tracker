package com.example.demo.Exception;

import org.springframework.http.HttpStatus;

/**
 * Represents a custom exception for handling user-related errors in the application.
 * This exception can be thrown to indicate various user-specific error conditions.
 */
public class UserException extends RuntimeException{
    private final HttpStatus status;

    public UserException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Creates a UserException indicating that the user with the specified ID was not found.
     *
     * @param id The identifier of the user that was not found
     * @return UserException with a message indicating the user was not found.
     */
    public static UserException userNotFound(Long id) {
        return new UserException(String.format("User with id %d not found.", id), HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a UserException indicating that the user with the specified username was already exists.
    * @param username The username that already exists
     * @return UserException with a message indicating the username already exists.
     */
    public static UserException usernameAlreadyExists(String username) {
        return new UserException(String.format("Username '%s' already exists.", username), HttpStatus.CONFLICT);
    }

    /**
     * Creates a UserException indicating that the user with the specified email was already exists.
     *
     * @param email The email that already exists
     * @return UserException with a message indicating the email already exists.
     */
    public static UserException emailAlreadyExists(String email) {
        return new UserException(String.format("Email '%s' already exists.", email), HttpStatus.CONFLICT);
    }

    /**
     * Creates a UserException indicating that the provided password is invalid.
     *
     * @return UserException with a message indicating the password is invalid.
     */
    public static UserException invalidPassword() {
        return new UserException("Invalid password provided.", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Creates a UserException indicating that the user ID is invalid.
     *
     * @param id The identifier of the user that is invalid
     * @return UserException with a message indicating the user ID is invalid.
     */
    public static UserException invalidUserId(Long id) {
        return new UserException(String.format("Invalid user ID: %d. It must be a positive number.", id), HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a UserException indicating that access to the user with the specified ID is denied.
     *
     * @param id The identifier of the user that the access is denied for
     * @return UserException with a message indicating access is denied.
     */
    public static UserException accessDenied(Long id) {
        return new UserException(String.format("Access denied for user with id %d.", id), HttpStatus.FORBIDDEN);
    }
    
    /**
     * Creates a UserException indicating that user data is required but not provided.
     *
     * @return UserException with a message indicating user data is required.
     */
    public static UserException userDataRequired() {
        return new UserException("User data is required", HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a UserException indicating that the username is required but not provided.
     *
     * @return UserException with a message indicating the username is required.
     */
    public static UserException usernameRequired() {
        return new UserException("Username is required and cannot be empty", HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a UserException indicating that the email is required but not provided.
     *
     * @return UserException with a message indicating the email is required.
     */
    public static UserException emailRequired() {
        return new UserException("Email is required and cannot be empty", HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a UserException indicating that the email format is invalid.
     *
     * @return UserException with a message indicating the email format is invalid.
     */
    public static UserException invalidEmailFormat() {
        return new UserException("Invalid email format", HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a UserException indicating that the password is required but not provided.
     *
     * @return UserException with a message indicating the password is required.
     */
    public static UserException passwordRequired() {
        return new UserException("Password is required and cannot be empty", HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Creates a UserException indicating that the password is too short.
     *
     * @return UserException with a message indicating the password must be at least 6 characters long.
     */
    public static UserException passwordTooShort() {
        return new UserException("Password must be at least 6 characters long", HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates a UserException indicating that the current user was not found.
     *
     * @return UserException with a message indicating the current user was not found.
     */
    public static UserException currentUserNotFound() {
        return new UserException("Current user not found", HttpStatus.NOT_FOUND);
    }
}