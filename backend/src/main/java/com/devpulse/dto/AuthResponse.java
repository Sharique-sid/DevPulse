package com.devpulse.dto;

public class AuthResponse {

    private String token;
    private Long orgId;
    private String email;
    private String role;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long orgId, String email, String role) {
        this.token = token;
        this.orgId = orgId;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
