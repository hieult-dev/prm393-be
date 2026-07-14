package com.myfschool.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Số điện thoại hoặc mật khẩu không đúng");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
