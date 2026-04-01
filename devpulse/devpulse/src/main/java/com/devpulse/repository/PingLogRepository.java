package com.devpulse.repository;

import com.devpulse.entity.PingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PingLogRepository extends JpaRepository<PingLog, Long> {

    List<PingLog> findByOrgIdAndEndpointId(Long orgId, Long endpointId);

    List<PingLog> findByOrgIdAndEndpointIdAndCheckedAtAfter(Long orgId, Long endpointId, LocalDateTime checkedAfter);

    List<PingLog> findByOrgId(Long orgId);

    List<PingLog> findByOrgIdAndCheckedAtAfter(Long orgId, LocalDateTime checkedAfter);

    @Query("SELECT COUNT(p) FROM PingLog p WHERE p.orgId = :orgId AND p.status = 'UP'")
    Long countSuccessfulPings(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(p) FROM PingLog p WHERE p.orgId = :orgId AND p.status = 'DOWN'")
    Long countFailedPings(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(p) FROM PingLog p WHERE p.orgId = :orgId")
    Long countTotalPings(@Param("orgId") Long orgId);

    @Query("SELECT AVG(p.responseTimeMs) FROM PingLog p WHERE p.orgId = :orgId AND p.status = 'UP'")
    Double getAverageResponseTime(@Param("orgId") Long orgId);

    @Query("SELECT p FROM PingLog p WHERE p.orgId = :orgId ORDER BY p.checkedAt DESC")
    List<PingLog> findLatestPingsByOrg(@Param("orgId") Long orgId);
}
