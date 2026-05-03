package com.devpulse.service;

import com.devpulse.dto.CreateEndpointRequest;
import com.devpulse.dto.EndpointResponse;
import com.devpulse.dto.UpdateEndpointRequest;
import com.devpulse.entity.Endpoint;
import com.devpulse.repository.EndpointRepository;
import com.devpulse.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EndpointService {

    private final EndpointRepository endpointRepository;

    public EndpointService(EndpointRepository endpointRepository) {
        this.endpointRepository = endpointRepository;
    }

    @Transactional
    public EndpointResponse createEndpoint(CreateEndpointRequest request) {
        Long orgId = SecurityUtils.getCurrentOrgId();

        Endpoint endpoint = new Endpoint();
        endpoint.setOrgId(orgId);
        endpoint.setName(request.getName().trim());
        endpoint.setUrl(request.getUrl().trim());
        endpoint.setMethod(request.getMethod().toUpperCase());
        endpoint.setCheckIntervalMinutes(request.getCheckIntervalMinutes());
        endpoint.setExpectedKeyword(request.getExpectedKeyword() != null ? request.getExpectedKeyword().trim() : null);
        endpoint.setIsActive(true);

        Endpoint saved = endpointRepository.save(endpoint);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EndpointResponse> getAllEndpoints() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return endpointRepository.findByOrgId(orgId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EndpointResponse getEndpointById(Long id) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        Endpoint endpoint = endpointRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Endpoint not found"
                ));
        return mapToResponse(endpoint);
    }

    @Transactional
    public EndpointResponse updateEndpoint(Long id, UpdateEndpointRequest request) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        Endpoint endpoint = endpointRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Endpoint not found"
                ));

        if (request.getName() != null && !request.getName().isBlank()) {
            endpoint.setName(request.getName().trim());
        }
        if (request.getUrl() != null && !request.getUrl().isBlank()) {
            endpoint.setUrl(request.getUrl().trim());
        }
        if (request.getMethod() != null && !request.getMethod().isBlank()) {
            endpoint.setMethod(request.getMethod().toUpperCase());
        }
        if (request.getCheckIntervalMinutes() != null) {
            endpoint.setCheckIntervalMinutes(request.getCheckIntervalMinutes());
        }
        if (request.getExpectedKeyword() != null) {
            endpoint.setExpectedKeyword(request.getExpectedKeyword().isBlank() ? null : request.getExpectedKeyword().trim());
        }
        if (request.getIsActive() != null) {
            endpoint.setIsActive(request.getIsActive());
        }

        Endpoint updated = endpointRepository.save(endpoint);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEndpoint(Long id) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        Endpoint endpoint = endpointRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Endpoint not found"
                ));
        endpointRepository.delete(endpoint);
    }

    private EndpointResponse mapToResponse(Endpoint endpoint) {
        return new EndpointResponse(
                endpoint.getId(),
                endpoint.getName(),
                endpoint.getUrl(),
                endpoint.getMethod(),
                endpoint.getCheckIntervalMinutes(),
                endpoint.getIsActive(),
                endpoint.getExpectedKeyword(),
                endpoint.getCreatedAt(),
                endpoint.getUpdatedAt()
        );
    }
}
