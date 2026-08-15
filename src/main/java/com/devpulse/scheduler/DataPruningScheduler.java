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
        int deletedPingLogs;
        int totalPingLogs = 0;
        do {
            deletedPingLogs = pingLogRepository.deleteOldPingLogsInBatches(pingLogCutoff);
            totalPingLogs += deletedPingLogs;
        } while (deletedPingLogs > 0);
        logger.info("Pruned {} ping logs older than {}", totalPingLogs, pingLogCutoff);

        // Prune Resolved Alerts older than 30 days
        LocalDateTime alertsCutoff = LocalDateTime.now().minusDays(ALERTS_RETENTION_DAYS);
        int deletedAlerts;
        int totalAlerts = 0;
        do {
            deletedAlerts = alertRepository.deleteOldResolvedAlertsInBatches(alertsCutoff);
            totalAlerts += deletedAlerts;
        } while (deletedAlerts > 0);
        logger.info("Pruned {} resolved alerts older than {}", totalAlerts, alertsCutoff);

        logger.info("Data pruning job completed.");
    }
}
