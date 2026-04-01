package com.devpulse.repository;

import com.devpulse.entity.AiInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiInsightRepository extends JpaRepository<AiInsight, Long> {

    List<AiInsight> findByOrgIdOrderByGeneratedAtDesc(Long orgId);

    Optional<AiInsight> findTopByOrgIdOrderByGeneratedAtDesc(Long orgId);
}
