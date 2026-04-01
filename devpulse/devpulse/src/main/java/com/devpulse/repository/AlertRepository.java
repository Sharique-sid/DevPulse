package com.devpulse.repository;

import com.devpulse.entity.Alert;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByOrgId(Long orgId);

    List<Alert> findByOrgIdAndIsResolved(Long orgId, Boolean isResolved);

    List<Alert> findByOrgIdAndEndpointId(Long orgId, Long endpointId);

    Optional<Alert> findByEndpointIdAndIsResolvedFalseOrderByCreatedAtDesc(Long endpointId);

    @Query("SELECT a FROM Alert a WHERE a.endpointId = :endpointId AND a.isResolved = false AND a.lastTriggeredAt > :cooldownTime ORDER BY a.createdAt DESC")
    List<Alert> findRecentUnresolvedAlerts(@Param("endpointId") Long endpointId, @Param("cooldownTime") LocalDateTime cooldownTime, Pageable pageable);
}
