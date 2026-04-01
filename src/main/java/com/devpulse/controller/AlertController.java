package com.devpulse.controller;

import com.devpulse.dto.AlertResponse;
import com.devpulse.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAllAlerts(
            @RequestParam(name = "status", required = false) String status) {
        
        List<AlertResponse> alerts;
        
        if ("unresolved".equalsIgnoreCase(status)) {
            alerts = alertService.getUnresolvedAlertsForOrg();
        } else if ("resolved".equalsIgnoreCase(status)) {
            alerts = alertService.getResolvedAlertsForOrg();
        } else {
            alerts = alertService.getAllAlertsForOrg();
        }
        
        return ResponseEntity.ok(alerts);
    }
}
