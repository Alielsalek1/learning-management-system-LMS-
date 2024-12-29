package com.main.lms.exceptions;

public class NotFoundRunTimeException extends RuntimeException {
    public NotFoundRunTimeException(String messString) {
        super(messString);
    }
}
