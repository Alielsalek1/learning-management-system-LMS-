package com.main.lms.exceptions;
import java.util.Optional;

public class InvalidUser extends RuntimeException {
    public InvalidUser(String message) {
        super(message);
    }
}
