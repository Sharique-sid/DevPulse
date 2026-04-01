package com.devpulse.controller;

import com.devpulse.dto.CreateEndpointRequest;
import com.devpulse.dto.EndpointResponse;
import com.devpulse.dto.UpdateEndpointRequest;
import com.devpulse.service.EndpointService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/endpoints")
public class EndpointController {

    private final EndpointService endpointService;

    public EndpointController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @PostMapping
    public ResponseEntity<EndpointResponse> createEndpoint(@Valid @RequestBody CreateEndpointRequest request) {
        EndpointResponse response = endpointService.createEndpoint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EndpointResponse>> getAllEndpoints() {
        List<EndpointResponse> endpoints = endpointService.getAllEndpoints();
        return ResponseEntity.ok(endpoints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EndpointResponse> getEndpointById(@PathVariable Long id) {
        EndpointResponse endpoint = endpointService.getEndpointById(id);
        return ResponseEntity.ok(endpoint);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EndpointResponse> updateEndpoint(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateEndpointRequest request) {
        EndpointResponse updated = endpointService.updateEndpoint(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable Long id) {
        endpointService.deleteEndpoint(id);
        return ResponseEntity.noContent().build();
    }
}
