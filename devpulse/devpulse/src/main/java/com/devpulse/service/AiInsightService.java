package com.devpulse.service;

import com.devpulse.dto.AiInsightResponse;
import com.devpulse.entity.AiInsight;
import com.devpulse.entity.Endpoint;
import com.devpulse.entity.PingLog;
import com.devpulse.repository.AiInsightRepository;
import com.devpulse.repository.EndpointRepository;
import com.devpulse.repository.PingLogRepository;
import com.devpulse.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AiInsightService {

    private final AiInsightRepository aiInsightRepository;
    private final EndpointRepository endpointRepository;
    private final PingLogRepository pingLogRepository;
    private final RestTemplate restTemplate;

    @Value("${app.ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiBaseUrl;

    public AiInsightService(AiInsightRepository aiInsightRepository,
                            EndpointRepository endpointRepository,
                            PingLogRepository pingLogRepository,
                            RestTemplate restTemplate) {
        this.aiInsightRepository = aiInsightRepository;
        this.endpointRepository = endpointRepository;
        this.pingLogRepository = pingLogRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public AiInsightResponse generateWeeklyInsight() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        String summaryInput = buildWeeklySummaryInput(orgId);
        String generatedText = generateWithGemini(summaryInput);

        AiInsight aiInsight = new AiInsight();
        aiInsight.setOrgId(orgId);
        aiInsight.setInsightText(generatedText);

        AiInsight saved = aiInsightRepository.save(aiInsight);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AiInsightResponse getLatestInsight() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        AiInsight latest = aiInsightRepository.findTopByOrgIdOrderByGeneratedAtDesc(orgId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No AI insight found for this organisation"));
        return toResponse(latest);
    }

    @Transactional(readOnly = true)
    public List<AiInsightResponse> getAllInsights() {
        Long orgId = SecurityUtils.getCurrentOrgId();
        return aiInsightRepository.findByOrgIdOrderByGeneratedAtDesc(orgId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private String buildWeeklySummaryInput(Long orgId) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Endpoint> endpoints = endpointRepository.findByOrgId(orgId);
        List<PingLog> recentLogs = pingLogRepository.findByOrgIdAndCheckedAtAfter(orgId, oneWeekAgo);

        long totalChecks = recentLogs.size();
        long upChecks = recentLogs.stream().filter(log -> "UP".equalsIgnoreCase(log.getStatus())).count();
        long downChecks = totalChecks - upChecks;
        double uptime = totalChecks == 0 ? 0.0 : ((double) upChecks / totalChecks) * 100.0;
        double avgResponse = recentLogs.stream()
                .filter(log -> "UP".equalsIgnoreCase(log.getStatus()) && log.getResponseTimeMs() != null)
                .mapToLong(PingLog::getResponseTimeMs)
                .average()
                .orElse(0.0);

        String endpointsList = endpoints.stream()
                .map(e -> "- " + e.getName() + " (" + e.getMethod() + " " + e.getUrl() + ")")
                .collect(Collectors.joining("\n"));

        String promptTemplate = """
            You are an API reliability assistant. Generate a short weekly health summary (4-6 lines) for an engineering team.
            Keep it actionable and include one improvement suggestion.

            Org ID: %d
            Total endpoints: %d
            Total checks (7d): %d
            Successful checks: %d
            Failed checks: %d
            Uptime %%: %.2f
            Average response time (ms): %.2f

            Endpoints:
            %s
            """;

        return String.format(
            promptTemplate,
            orgId,
            endpoints.size(),
            totalChecks,
            upChecks,
            downChecks,
            uptime,
            avgResponse,
            endpointsList.isBlank() ? "- No endpoints configured" : endpointsList
        );
    }

    private String generateWithGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is missing. Set app.ai.gemini.api-key or GEMINI_API_KEY.");
        }

        String url = geminiBaseUrl + "/" + geminiModel + ":generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {
            }
        );

        Object responseBody = response.getBody();
        if (!(responseBody instanceof Map<?, ?> root)) {
            throw new IllegalStateException("Gemini response body is empty or invalid");
        }

        List<?> candidates = castList(root.get("candidates"));
        if (candidates.isEmpty() || !(candidates.get(0) instanceof Map<?, ?> firstCandidate)) {
            throw new IllegalStateException("Gemini response did not contain candidates");
        }

        Map<?, ?> content = castMap(firstCandidate.get("content"));
        List<?> parts = castList(content.get("parts"));
        if (parts.isEmpty() || !(parts.get(0) instanceof Map<?, ?> firstPart)) {
            throw new IllegalStateException("Gemini response did not contain text parts");
        }

        Object text = firstPart.get("text");
        if (text == null || text.toString().isBlank()) {
            throw new IllegalStateException("Gemini response did not contain insight text");
        }

        return text.toString().trim();
    }

    private AiInsightResponse toResponse(AiInsight insight) {
        return new AiInsightResponse(insight.getId(), insight.getInsightText(), insight.getGeneratedAt());
    }

    private List<?> castList(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        return new ArrayList<>();
    }

    private Map<?, ?> castMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        return Map.of();
    }
}
