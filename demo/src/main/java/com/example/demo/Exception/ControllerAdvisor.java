package com.example.demo.Exception; 

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {
   private static final Logger logger = LoggerFactory.getLogger(ControllerAdvisor.class);

    private ResponseEntity<Object> info(Exception ex, WebRequest request, String customMessage) {
        HttpStatus status;
        status = getStatus(ex);
        logger.error("Exception caught: status={}, message={}, path={}",
        status, customMessage != null ? customMessage : ex.getMessage(), request.getDescription(false), ex);
        
        //Error message | Type of error
        //      ⋁        ⋁
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", errorType(status));
        body.put("message", customMessage != null ? customMessage : ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, status);
    }


    private HttpStatus getStatus(Exception ex) {
        if (ex instanceof GoalException) {                   return ((GoalException) ex).getStatus();
        } else if (ex instanceof UserException) {            return ((UserException) ex).getStatus();
        } else if (ex instanceof WorkoutException) {         return ((WorkoutException) ex).getStatus();
        } else if (ex instanceof StatisticsException) {      return ((StatisticsException) ex).getStatus();
        } else if (ex instanceof AdminException){            return ((AdminException) ex).getStatus();    
        } else if (ex instanceof SecurityException) {        return HttpStatus.FORBIDDEN;
        } else if (ex instanceof IllegalArgumentException) { return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof DateTimeParseException) {   return HttpStatus.BAD_REQUEST;
        } else {                                             return HttpStatus.INTERNAL_SERVER_ERROR; }
    }

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<Object> handleAdminException(AdminException ex, WebRequest request) {
        return info(ex, request, null);
    }
    
    @ExceptionHandler(UserException.class)
    public ResponseEntity<Object> handleUserException(UserException ex, WebRequest request) {
        return info(ex, request, null);
    }

    @ExceptionHandler(WorkoutException.class)
    public ResponseEntity<Object> handleWorkoutException(WorkoutException ex, WebRequest request) {
        return info(ex, request, null);
    }


    @ExceptionHandler(StatisticsException.class)
    public ResponseEntity<Object> handleStatisticsException(StatisticsException ex, WebRequest request) {
        return info(ex, request, null);
    }

     @ExceptionHandler(GoalException.class)
    public ResponseEntity<Object> handleGoalException(GoalException ex, WebRequest request) {
        return info(ex, request, null);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Object> handleSecurityException(SecurityException ex, WebRequest request) {
        return info(ex, request, "You don't have permission to access this goal");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return info(ex, request, null);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) {
        return info(ex, request, "Please use format YYYY-MM-DD and ensure the date is valid");
    }

    private ResponseEntity<Object> buildBadRequestResponse(String message, WebRequest request) {
        IllegalArgumentException ex = new IllegalArgumentException(message);
        return info(ex, request, message);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = "Invalid request format";

        if (ex.getCause() instanceof InvalidFormatException ife) {
            if ("LocalDate".equals(ife.getTargetType().getSimpleName())) {
                message = String.format("Invalid date format: '%s'. Please use format YYYY-MM-DD.", ife.getValue());
                message += String.format(" details: %s", ex.getMessage());
            } else {
                message = String.format("Invalid format for field '%s': %s", getFieldName(ife), ife.getValue());
                message += String.format(" details: %s", ex.getMessage());
            }
        } else if (ex.getCause() instanceof JsonMappingException jme) {
            message = "Invalid JSON format: " + jme.getOriginalMessage();
        }

        return buildBadRequestResponse(message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String paramName = ex.getName();
        String paramValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        String message;
        if ("LocalDate".equals(requiredType)) {
            message = String.format("Invalid date format for parameter '%s': '%s'. Please use YYYY-MM-DD.", paramName,
                    paramValue);
                    message += String.format(" details: %s", ex.getMessage());
        } else {
            message = String.format("Invalid format for parameter '%s': '%s'. Expected type: %s", paramName, paramValue,
                    requiredType);
                    message += String.format(" details: %s", ex.getMessage());
        }

        return buildBadRequestResponse(message, request);
    }


   private String errorType(HttpStatus status) {
        switch (status) {
            case NOT_FOUND:             return "Not Found";
            case BAD_REQUEST:           return "Bad Request";
            case FORBIDDEN:             return "Forbidden";
            case INTERNAL_SERVER_ERROR: return "Internal Server Error";
            case CONFLICT:              return "Conflict";
            case UNAUTHORIZED:          return "Unauthorized";
            default:                    return "Internal Server Error";
        }
    }

    private String getFieldName(InvalidFormatException ex) {
        if (ex.getPath() != null && !ex.getPath().isEmpty()) {
            return ex.getPath().get(ex.getPath().size() - 1).getFieldName();
        }
        return "unknown field";
    }
}