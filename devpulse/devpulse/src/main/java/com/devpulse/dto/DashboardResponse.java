package com.devpulse.dto;

public class DashboardResponse {

    private Long totalEndpoints;
    private Long activeEndpoints;
    private Double uptimePercentage;
    private Double avgResponseTimeMs;
    private Long totalPinged;
    private Long successfulPings;
    private Long failedPings;

    public DashboardResponse() {
    }

    public DashboardResponse(Long totalEndpoints, Long activeEndpoints, Double uptimePercentage,
                           Double avgResponseTimeMs, Long totalPinged, Long successfulPings, Long failedPings) {
        this.totalEndpoints = totalEndpoints;
        this.activeEndpoints = activeEndpoints;
        this.uptimePercentage = uptimePercentage;
        this.avgResponseTimeMs = avgResponseTimeMs;
        this.totalPinged = totalPinged;
        this.successfulPings = successfulPings;
        this.failedPings = failedPings;
    }

    public Long getTotalEndpoints() {
        return totalEndpoints;
    }

    public void setTotalEndpoints(Long totalEndpoints) {
        this.totalEndpoints = totalEndpoints;
    }

    public Long getActiveEndpoints() {
        return activeEndpoints;
    }

    public void setActiveEndpoints(Long activeEndpoints) {
        this.activeEndpoints = activeEndpoints;
    }

    public Double getUptimePercentage() {
        return uptimePercentage;
    }

    public void setUptimePercentage(Double uptimePercentage) {
        this.uptimePercentage = uptimePercentage;
    }

    public Double getAvgResponseTimeMs() {
        return avgResponseTimeMs;
    }

    public void setAvgResponseTimeMs(Double avgResponseTimeMs) {
        this.avgResponseTimeMs = avgResponseTimeMs;
    }

    public Long getTotalPinged() {
        return totalPinged;
    }

    public void setTotalPinged(Long totalPinged) {
        this.totalPinged = totalPinged;
    }

    public Long getSuccessfulPings() {
        return successfulPings;
    }

    public void setSuccessfulPings(Long successfulPings) {
        this.successfulPings = successfulPings;
    }

    public Long getFailedPings() {
        return failedPings;
    }

    public void setFailedPings(Long failedPings) {
        this.failedPings = failedPings;
    }
}
