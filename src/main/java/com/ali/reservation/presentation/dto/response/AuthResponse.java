package com.ali.reservation.presentation.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private static final String BEARER = "Bearer";
    private String token;
    private String type = BEARER;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
    }
}
