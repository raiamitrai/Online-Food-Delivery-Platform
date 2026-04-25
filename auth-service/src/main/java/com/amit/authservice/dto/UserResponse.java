package com.amit.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phone;
}