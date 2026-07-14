package com.myfschool.exception;

public class ResetTokenExpiredException extends RuntimeException {

    public ResetTokenExpiredException() {
        super("Reset token đã hết hạn");
    }
}
