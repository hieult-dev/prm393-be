package com.myfschool.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("Not found " + translateResourceName(resourceName) + " with id " + id);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("Not found " + translateResourceName(resourceName)
                + " with " + translateFieldName(fieldName) + " " + fieldValue);
    }

    private static String translateResourceName(String resourceName) {
        return switch (resourceName) {
            case "Application type" -> "application type";
            case "Club" -> "club";
            case "Club member" -> "club member";
            case "Schedule" -> "schedule";
            case "Event" -> "event";
            case "Student application" -> "student application";
            case "Student grade" -> "student grade";
            case "Semester" -> "semester";
            case "Password reset token" -> "password reset token";
            case "Subject" -> "subject";
            case "User" -> "user";
            case "Role" -> "role";
            case "Permission" -> "permission";
            default -> "data";
        };
    }

    private static String translateFieldName(String fieldName) {
        return switch (fieldName) {
            case "id" -> "id";
            case "phone" -> "phone";
            case "userName" -> "user_name";
            case "email" -> "email";
            case "roleName" -> "role_name";
            case "permissionName" -> "permission_name";
            default -> fieldName;
        };
    }
}
