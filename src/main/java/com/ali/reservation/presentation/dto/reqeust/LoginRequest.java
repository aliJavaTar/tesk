package com.ali.reservation.presentation.dto.reqeust;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @Size(min = 3, max = 50)
    @NotBlank(message = "Username is required")
    private String username;

    @Size(min = 7, max = 50)
    @NotBlank(message = "Password is required")
    private String password;
}
