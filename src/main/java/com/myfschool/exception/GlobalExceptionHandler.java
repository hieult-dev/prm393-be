package com.myfschool.exception;

import com.myfschool.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> translateFieldName(error.getField()) + ": " + translateValidationMessage(error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid data"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid request body"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Missing parameter " + translateFieldName(exception.getParameterName())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Parameter " + translateFieldName(exception.getName()) + " has invalid format"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("HTTP method is not supported"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("Content type is not supported"));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Path not found"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Data violates constraints"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("System error"));
    }

    private String translateFieldName(String fieldName) {
        return switch (fieldName) {
            case "userName" -> "user_name";
            case "firstName" -> "first_name";
            case "lastName" -> "last_name";
            case "email" -> "email";
            case "userPassword", "password" -> "user_password";
            case "phone" -> "phone";
            case "className" -> "class_name";
            case "role" -> "role";
            case "roleName" -> "role_name";
            case "permissionName" -> "permission_name";
            case "status" -> "status";
            case "name" -> "name";
            case "schoolYear" -> "school_year";
            case "subjectCode" -> "subject_code";
            case "subjectName" -> "subject_name";
            case "credits" -> "credits";
            case "userId" -> "user_id";
            case "subjectId" -> "subject_id";
            case "semesterId" -> "semester_id";
            case "totalScore" -> "total_score";
            case "letterGrade" -> "letter_grade";
            case "studyDate" -> "study_date";
            case "startTime" -> "start_time";
            case "endTime" -> "end_time";
            case "room" -> "room";
            case "lecturerName" -> "lecturer_name";
            case "title" -> "title";
            case "content" -> "content";
            case "applicationTypeId" -> "application_type_id";
            case "responseNote" -> "response_note";
            case "clubId" -> "club_id";
            case "token" -> "token";
            case "expiredAt" -> "expired_at";
            default -> fieldName;
        };
    }

    private String translateValidationMessage(String message) {
        if (message == null) {
            return "is invalid";
        }
        return switch (message) {
            case "must not be blank" -> "must not be blank";
            case "must not be null" -> "must not be null";
            case "must be a well-formed email address" -> "must be a valid email";
            default -> message;
        };
    }
}
