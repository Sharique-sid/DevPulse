package com.devpulse.scheduler;

import com.devpulse.repository.AlertRepository;
import com.devpulse.repository.PingLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataPruningScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataPruningScheduler.class);
    
    // Retention periods
    private static final int PING_LOGS_RETENTION_DAYS = 7;
    private static final int ALERTS_RETENTION_DAYS = 30;

    private final PingLogRepository pingLogRepository;
    private final AlertRepository alertRepository;

    public DataPruningScheduler(PingLogRepository pingLogRepository, AlertRepository alertRepository) {
        this.pingLogRepository = pingLogRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Runs every midnight (cron = "0 0 0 * * ?")
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void pruneOldData() {
        logger.info("Starting scheduled data pruning job...");

        // Prune Ping Logs older than 7 days
        LocalDateTime pingLogCutoff = LocalDateTime.now().minusDays(PING_LOGS_RETENTION_DAYS);
        pingLogRepository.deleteByCheckedAtBefore(pingLogCutoff);
        logger.info("Pruned ping logs older than {}", pingLogCutoff);

        // Prune Resolved Alerts older than 30 days
        LocalDateTime alertsCutoff = LocalDateTime.now().minusDays(ALERTS_RETENTION_DAYS);
        alertRepository.deleteByIsResolvedTrueAndCreatedAtBefore(alertsCutoff);
        logger.info("Pruned resolved alerts older than {}", alertsCutoff);

        logger.info("Data pruning job completed.");
    }
}
