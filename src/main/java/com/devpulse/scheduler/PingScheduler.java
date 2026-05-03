package com.devpulse.scheduler;

import com.devpulse.entity.Alert;
import com.devpulse.entity.Endpoint;
import com.devpulse.entity.PingLog;
import com.devpulse.repository.AlertRepository;
import com.devpulse.repository.EndpointRepository;
import com.devpulse.repository.PingLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PingScheduler.class);
    private static final long ALERT_COOLDOWN_MINUTES = 5;

    private final EndpointRepository endpointRepository;
    private final PingLogRepository pingLogRepository;
    private final AlertRepository alertRepository;
    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public PingScheduler(EndpointRepository endpointRepository,
                         PingLogRepository pingLogRepository,
                         AlertRepository alertRepository,
                         RestTemplate restTemplate,
                         SimpMessagingTemplate messagingTemplate) {
        this.endpointRepository = endpointRepository;
        this.pingLogRepository = pingLogRepository;
        this.alertRepository = alertRepository;
        this.restTemplate = restTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void pingAllEndpoints() {
        logger.info("Starting scheduled ping job...");

        List<Endpoint> activeEndpoints = endpointRepository.findAll()
                .stream()
                .filter(e -> e.getIsActive() != null && e.getIsActive())
                .toList();

        logger.info("Found {} active endpoints to ping", activeEndpoints.size());

        for (Endpoint endpoint : activeEndpoints) {
            try {
                pingEndpoint(endpoint);
            } catch (RuntimeException ex) {
                logger.error("Error pinging endpoint {}: {}", endpoint.getId(), ex.getMessage());
            }
        }

        logger.info("Ping job completed");
    }

    private void pingEndpoint(Endpoint endpoint) {
        long startTime = System.currentTimeMillis();
        PingLog pingLog = new PingLog();
        pingLog.setEndpointId(endpoint.getId());
        pingLog.setOrgId(endpoint.getOrgId());

        try {
            HttpMethod httpMethod = HttpMethod.valueOf(endpoint.getMethod().toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(endpoint.getUrl(), httpMethod, null, String.class);

            if (endpoint.getExpectedKeyword() != null && !endpoint.getExpectedKeyword().isBlank()) {
                String body = response.getBody();
                if (body == null || !body.contains(endpoint.getExpectedKeyword())) {
                    throw new RuntimeException("Response did not contain expected keyword: '" + endpoint.getExpectedKeyword() + "'");
                }
            }

            long responseTime = System.currentTimeMillis() - startTime;
            pingLog.setStatus("UP");
            pingLog.setResponseTimeMs(responseTime);
            pingLog.setStatusCode(200);

            logger.debug("Endpoint {} is UP (response time: {}ms)", endpoint.getId(), responseTime);

            pingLogRepository.save(pingLog);
            messagingTemplate.convertAndSend("/topic/org/" + endpoint.getOrgId() + "/pings", pingLog);
            resolveAlertIfExists(endpoint.getId());

        } catch (RestClientException ex) {
            long responseTime = System.currentTimeMillis() - startTime;
            pingLog.setStatus("DOWN");
            pingLog.setResponseTimeMs(responseTime);
            pingLog.setStatusCode(0);

            logger.warn("Endpoint {} is DOWN: {}", endpoint.getId(), ex.getMessage());

            pingLogRepository.save(pingLog);
            messagingTemplate.convertAndSend("/topic/org/" + endpoint.getOrgId() + "/pings", pingLog);
            createAlertIfNeeded(endpoint, ex.getMessage());
        } catch (RuntimeException ex) {
            long responseTime = System.currentTimeMillis() - startTime;
            pingLog.setStatus("DOWN");
            pingLog.setResponseTimeMs(responseTime);
            pingLog.setStatusCode(0);

            logger.warn("Endpoint {} failed with runtime error: {}", endpoint.getId(), ex.getMessage());

            pingLogRepository.save(pingLog);
            messagingTemplate.convertAndSend("/topic/org/" + endpoint.getOrgId() + "/pings", pingLog);
            createAlertIfNeeded(endpoint, ex.getMessage());
        }
    }

    private void createAlertIfNeeded(Endpoint endpoint, String errorMessage) {
        LocalDateTime cooldownThreshold = LocalDateTime.now().minusMinutes(ALERT_COOLDOWN_MINUTES);

        alertRepository.findRecentUnresolvedAlerts(endpoint.getId(), cooldownThreshold, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .ifPresentOrElse(
                        existingAlert -> {
                            existingAlert.setLastTriggeredAt(LocalDateTime.now());
                            alertRepository.save(existingAlert);
                            logger.debug("Updated existing alert for endpoint {}", endpoint.getId());
                        },
                        () -> {
                            Alert newAlert = new Alert();
                            newAlert.setEndpointId(endpoint.getId());
                            newAlert.setOrgId(endpoint.getOrgId());
                            newAlert.setMessage("Endpoint \"" + endpoint.getName() + "\" is down. Error: " + errorMessage);
                            newAlert.setIsResolved(false);
                            alertRepository.save(newAlert);
                            messagingTemplate.convertAndSend("/topic/org/" + endpoint.getOrgId() + "/alerts", newAlert);
                            logger.info("Created new alert for endpoint {}", endpoint.getId());
                        }
                );
    }

    private void resolveAlertIfExists(Long endpointId) {
        alertRepository.findByEndpointIdAndIsResolvedFalseOrderByCreatedAtDesc(endpointId)
                .ifPresent(alert -> {
                    alert.setIsResolved(true);
                    alertRepository.save(alert);
                    messagingTemplate.convertAndSend("/topic/org/" + alert.getOrgId() + "/alerts", alert);
                    logger.info("Resolved alert for endpoint {}", endpointId);
                });
    }
}
