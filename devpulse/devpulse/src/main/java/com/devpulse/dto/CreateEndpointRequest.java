package com.devpulse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateEndpointRequest {

    @NotBlank(message = "Endpoint name is required")
    @Size(max = 120, message = "Endpoint name must be at most 120 characters")
    private String name;

    @NotBlank(message = "URL is required")
    @Size(max = 500, message = "URL must be at most 500 characters")
    private String url;

    @NotBlank(message = "HTTP method is required")
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS", message = "HTTP method must be one of: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS")
    private String method;

    @NotNull(message = "Check interval (in minutes) is required")
    @Min(value = 1, message = "Check interval must be at least 1 minute")
    private Integer checkIntervalMinutes;

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
}
