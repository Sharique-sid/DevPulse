package com.devpulse.security;

public class UserPrincipal {

    private final Long orgId;
    private final String email;
    private final String role;

    public UserPrincipal(Long orgId, String email, String role) {
        this.orgId = orgId;
        this.email = email;
        this.role = role;
    }

    public Long getOrgId() {
        return orgId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
