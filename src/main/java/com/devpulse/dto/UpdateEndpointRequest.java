package com.devpulse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateEndpointRequest {

    @Size(max = 120, message = "Endpoint name must be at most 120 characters")
    private String name;

    @Size(max = 500, message = "URL must be at most 500 characters")
    private String url;

    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS", message = "HTTP method must be one of: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS")
    private String method;

    @Min(value = 1, message = "Check interval must be at least 1 minute")
    private Integer checkIntervalMinutes;

    private Boolean isActive;

    @Size(max = 100, message = "Expected keyword must be at most 100 characters")
    private String expectedKeyword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getCheckIntervalMinutes() {
        return checkIntervalMinutes;
    }

    public void setCheckIntervalMinutes(Integer checkIntervalMinutes) {
        this.checkIntervalMinutes = checkIntervalMinutes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getExpectedKeyword() {
        return expectedKeyword;
    }

    public void setExpectedKeyword(String expectedKeyword) {
        this.expectedKeyword = expectedKeyword;
    }
}
