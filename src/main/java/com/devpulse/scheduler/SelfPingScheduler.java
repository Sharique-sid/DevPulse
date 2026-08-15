package com.devpulse.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SelfPingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SelfPingScheduler.class);
    private final RestTemplate restTemplate;

    @Value("${app.self.url:${RENDER_EXTERNAL_URL:}}")
    private String selfUrl;

    public SelfPingScheduler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Ping every 14 minutes (840,000 ms) to keep the Render free tier instance awake
    @Scheduled(fixedRate = 840000)
    public void keepAwake() {
        if (selfUrl != null && !selfUrl.trim().isEmpty()) {
            logger.info("Pinging self to keep awake: {}", selfUrl);
            try {
                restTemplate.getForObject(selfUrl, String.class);
                logger.info("Self-ping successful");
            } catch (Exception e) {
                logger.warn("Self-ping failed: {}", e.getMessage());
            }
        } else {
            logger.debug("Self-ping URL not configured (app.self.url or RENDER_EXTERNAL_URL is empty)");
        }
    }
}
