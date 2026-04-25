package com.amit.authservice.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String login;
    private String password;
}