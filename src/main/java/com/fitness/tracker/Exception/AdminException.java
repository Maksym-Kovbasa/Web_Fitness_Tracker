package com.fitness.tracker.Exception;

import org.springframework.http.HttpStatus;

public class AdminException extends RuntimeException {
    private final HttpStatus status;

    public AdminException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Creates an AdminException indicating that the user is not an admin.
     * @return AdminException with a message and HTTP status 403 Forbidden.
     */
    public static AdminException userNotAdmin() {
        return new AdminException(String.format("Access denied: User with is not an admin."), HttpStatus.FORBIDDEN);
    }

    /**
     * Creates an AdminException indicating that the users is empty.
     * @return AdminException with a message and HTTP status 404 Not Found.
     */
    public static AdminException usersNotFound(){
        return new AdminException("Users not found because users data is empty.", HttpStatus.NOT_FOUND);
    }


    /**
     * Creates an AdminException indicating that the user with id not present.
     * @param id id The identifier of the user that is invalid
     * @return AdminException with a message and HTTP status 404 Not Found.
     */
    public static AdminException userNotFound(Long id){
        return new AdminException("User with id %id not found.", HttpStatus.NOT_FOUND);
    }

    /**
     * Creates an AdminException indicating that the admin try delete own account.
     * @returnAdminException with a message and HTTP status 403 Forbiden.
     */
    public static AdminException cannotDeleteOwnAccount() {
        return new AdminException("You cannot delete your own account.", HttpStatus.FORBIDDEN);
    }

    public static AdminException userHasNoGoals() {
        return new AdminException("The user has no goals to delete.", HttpStatus.NOT_FOUND);
    }


    public static AdminException goalNotFound(Long id) {
        return new AdminException(String.format("Goal with ID %d not found.", id), HttpStatus.NOT_FOUND);
    }

    public static AdminException goalEmpty() {
        return new AdminException("Goals not found because users data is empty.", HttpStatus.NOT_FOUND);
    }

    public static AdminException workoutEmpty() {
        return new AdminException("Workouts not found because users data is empty.", HttpStatus.NOT_FOUND);
    }
}
