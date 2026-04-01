package com.devpulse.service;

import com.devpulse.dto.AlertResponse;
import com.devpulse.entity.Alert;
import com.devpulse.repository.AlertRepository;
import com.devpulse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAllAlertsForOrg() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return alertRepository.findByOrgId(orgId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getUnresolvedAlertsForOrg() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return alertRepository.findByOrgIdAndIsResolved(orgId, false)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getResolvedAlertsForOrg() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return alertRepository.findByOrgIdAndIsResolved(orgId, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AlertResponse mapToResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getEndpointId(),
                alert.getMessage(),
                alert.getIsResolved(),
                alert.getCreatedAt(),
                alert.getLastTriggeredAt()
        );
    }
}
