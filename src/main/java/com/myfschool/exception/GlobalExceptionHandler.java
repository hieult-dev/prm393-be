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
        return ResponseEntity.badRequest().body(ApiResponse.error("Dữ liệu không hợp lệ"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Dữ liệu gửi lên không đúng định dạng"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Thiếu tham số " + translateFieldName(exception.getParameterName())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Tham số " + translateFieldName(exception.getName()) + " không đúng định dạng"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("Phương thức HTTP không được hỗ trợ"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("Định dạng nội dung không được hỗ trợ"));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Không tìm thấy đường dẫn"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Dữ liệu vi phạm ràng buộc"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Lỗi hệ thống"));
    }

    private String translateFieldName(String fieldName) {
        return switch (fieldName) {
            case "studentCode" -> "Mã sinh viên";
            case "fullName" -> "Họ và tên";
            case "email" -> "Email";
            case "passwordHash", "password" -> "Mật khẩu";
            case "phone" -> "Số điện thoại";
            case "className" -> "Lớp";
            case "role" -> "Vai trò";
            case "status" -> "Trạng thái";
            case "name" -> "Tên";
            case "schoolYear" -> "Năm học";
            case "subjectCode" -> "Mã môn học";
            case "subjectName" -> "Tên môn học";
            case "credits" -> "Số tín chỉ";
            case "userId" -> "Người dùng";
            case "subjectId" -> "Môn học";
            case "semesterId" -> "Học kỳ";
            case "totalScore" -> "Tổng điểm";
            case "letterGrade" -> "Điểm chữ";
            case "studyDate" -> "Ngày học";
            case "startTime" -> "Thời gian bắt đầu";
            case "endTime" -> "Thời gian kết thúc";
            case "room" -> "Phòng";
            case "lecturerName" -> "Giảng viên";
            case "title" -> "Tiêu đề";
            case "content" -> "Nội dung";
            case "applicationTypeId" -> "Loại đơn";
            case "responseNote" -> "Ghi chú phản hồi";
            case "clubId" -> "Câu lạc bộ";
            case "token" -> "Mã xác thực";
            case "expiredAt" -> "Thời gian hết hạn";
            default -> fieldName;
        };
    }

    private String translateValidationMessage(String message) {
        if (message == null) {
            return "không hợp lệ";
        }
        return switch (message) {
            case "must not be blank" -> "không được để trống";
            case "must not be null" -> "không được để trống";
            case "must be a well-formed email address" -> "không đúng định dạng";
            default -> message;
        };
    }
}
