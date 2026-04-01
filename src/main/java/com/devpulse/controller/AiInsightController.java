package com.devpulse.controller;

import com.devpulse.dto.AiInsightResponse;
import com.devpulse.service.AiInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai-insights")
public class AiInsightController {

    private final AiInsightService aiInsightService;

    public AiInsightController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @PostMapping("/generate")
    public ResponseEntity<AiInsightResponse> generateInsight() {
        return ResponseEntity.ok(aiInsightService.generateWeeklyInsight());
    }

    @GetMapping("/latest")
    public ResponseEntity<AiInsightResponse> getLatestInsight() {
        return ResponseEntity.ok(aiInsightService.getLatestInsight());
    }

    @GetMapping
    public ResponseEntity<List<AiInsightResponse>> getAllInsights() {
        return ResponseEntity.ok(aiInsightService.getAllInsights());
    }
}
