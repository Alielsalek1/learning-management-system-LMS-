package com.main.lms.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String errorMsg) {
        super(errorMsg);
    }
}
