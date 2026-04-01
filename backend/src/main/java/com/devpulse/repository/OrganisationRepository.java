package com.devpulse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devpulse.entity.Organisation;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Optional<Organisation> findByEmail(String email);

    boolean existsByEmail(String email);
}
