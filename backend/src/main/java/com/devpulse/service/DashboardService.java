package com.devpulse.service;

import com.devpulse.dto.DashboardResponse;
import com.devpulse.repository.EndpointRepository;
import com.devpulse.repository.PingLogRepository;
import com.devpulse.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final EndpointRepository endpointRepository;
    private final PingLogRepository pingLogRepository;

    public DashboardService(EndpointRepository endpointRepository, PingLogRepository pingLogRepository) {
        this.endpointRepository = endpointRepository;
        this.pingLogRepository = pingLogRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        Long orgId = SecurityUtils.getCurrentOrgId();

        long totalEndpoints = endpointRepository.findByOrgId(orgId).size();
        long activeEndpoints = endpointRepository.findByOrgIdAndIsActive(orgId, true).size();

        long successfulPings = pingLogRepository.countSuccessfulPings(orgId);
        long failedPings = pingLogRepository.countFailedPings(orgId);
        long totalPings = successfulPings + failedPings;

        double uptimePercentage = totalPings > 0 ? (double) successfulPings / totalPings * 100 : 0.0;
        Double avgResponseTime = pingLogRepository.getAverageResponseTime(orgId);
        if (avgResponseTime == null) {
            avgResponseTime = 0.0;
        }

        return new DashboardResponse(
                totalEndpoints,
                activeEndpoints,
                Math.round(uptimePercentage * 100.0) / 100.0,
                Math.round(avgResponseTime * 100.0) / 100.0,
                totalPings,
                successfulPings,
                failedPings
        );
    }
}
