package com.myfschool.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("Không tìm thấy " + translateResourceName(resourceName) + " với id " + id);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("Không tìm thấy " + translateResourceName(resourceName)
                + " với " + translateFieldName(fieldName) + " " + fieldValue);
    }

    private static String translateResourceName(String resourceName) {
        return switch (resourceName) {
            case "Application type" -> "loại đơn";
            case "Club" -> "câu lạc bộ";
            case "Club member" -> "thành viên câu lạc bộ";
            case "Schedule" -> "lịch học";
            case "Event" -> "sự kiện";
            case "Student application" -> "đơn sinh viên";
            case "Student grade" -> "điểm sinh viên";
            case "Semester" -> "học kỳ";
            case "Password reset token" -> "mã đặt lại mật khẩu";
            case "Subject" -> "môn học";
            case "User" -> "người dùng";
            default -> "dữ liệu";
        };
    }

    private static String translateFieldName(String fieldName) {
        return switch (fieldName) {
            case "id" -> "id";
            case "phone" -> "số điện thoại";
            case "studentCode" -> "mã sinh viên";
            case "email" -> "email";
            default -> fieldName;
        };
    }
}
