package com.devpulse.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devpulse.dto.AuthResponse;
import com.devpulse.dto.LoginRequest;
import com.devpulse.dto.RegisterRequest;
import com.devpulse.entity.Organisation;
import com.devpulse.repository.OrganisationRepository;
import com.devpulse.security.JwtUtil;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "ADMIN";

    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(OrganisationRepository organisationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.organisationRepository = organisationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (organisationRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Organisation with this email already exists");
        }

        Organisation organisation = new Organisation();
        organisation.setName(request.getName().trim());
        organisation.setEmail(normalizedEmail);
        organisation.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        organisation.setPlan("FREE");

        Organisation saved = organisationRepository.save(organisation);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), DEFAULT_ROLE);

        return new AuthResponse(token, saved.getId(), saved.getEmail(), DEFAULT_ROLE);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Organisation organisation = organisationRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), organisation.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(organisation.getId(), organisation.getEmail(), DEFAULT_ROLE);
        return new AuthResponse(token, organisation.getId(), organisation.getEmail(), DEFAULT_ROLE);
    }
}
