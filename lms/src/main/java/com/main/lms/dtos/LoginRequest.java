package com.main.lms.dtos;

import lombok.Data;

@Data
public class LoginRequest {

    private String name;
    private String password;

    public LoginRequest() {}

}
