package com.main.lms.dtos;

import lombok.Data;


@Data
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String[] errors;

    public ApiResponse(boolean success, String message, T data, String[] errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors != null ? errors : new String[0];
    }
    public ApiResponse() {
    }
}

