package com.devpulse.repository;

import com.devpulse.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EndpointRepository extends JpaRepository<Endpoint, Long> {

    List<Endpoint> findByOrgId(Long orgId);

    List<Endpoint> findByOrgIdAndIsActive(Long orgId, Boolean isActive);

    Optional<Endpoint> findByIdAndOrgId(Long id, Long orgId);

    @Query("SELECT e FROM Endpoint e WHERE e.orgId = :orgId")
    List<Endpoint> findAllByOrgId(@Param("orgId") Long orgId);

    boolean existsByIdAndOrgId(Long id, Long orgId);
}
