package com.devpulse.dto;

import java.time.LocalDateTime;

public class AlertResponse {

    private Long id;
    private Long endpointId;
    private String message;
    private Boolean isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime lastTriggeredAt;

    public AlertResponse() {
    }

    public AlertResponse(Long id, Long endpointId, String message, Boolean isResolved,
                        LocalDateTime createdAt, LocalDateTime lastTriggeredAt) {
        this.id = id;
        this.endpointId = endpointId;
        this.message = message;
        this.isResolved = isResolved;
        this.createdAt = createdAt;
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(Long endpointId) {
        this.endpointId = endpointId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsResolved() {
        return isResolved;
    }

    public void setIsResolved(Boolean isResolved) {
        this.isResolved = isResolved;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }
}
