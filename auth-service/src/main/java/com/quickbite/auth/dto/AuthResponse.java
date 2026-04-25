package com.quickbite.auth.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String message;
    private Long userId;
    private String role;

    public AuthResponse() {}
    public AuthResponse(String accessToken, String refreshToken, String message, Long userId, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
        this.userId = userId;
        this.role = role;
    }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public static AuthResponseBuilder builder() { return new AuthResponseBuilder(); }

    public static class AuthResponseBuilder {
        private String accessToken;
        private String refreshToken;
        private String message;
        private Long userId;
        private String role;
        public AuthResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public AuthResponseBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public AuthResponseBuilder message(String message) { this.message = message; return this; }
        public AuthResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public AuthResponseBuilder role(String role) { this.role = role; return this; }
        public AuthResponse build() { return new AuthResponse(accessToken, refreshToken, message, userId, role); }
    }
}
