package com.devpulse.dto;

import java.time.LocalDateTime;

public class AiInsightResponse {

    private Long id;
    private String insightText;
    private LocalDateTime generatedAt;

    public AiInsightResponse() {
    }

    public AiInsightResponse(Long id, String insightText, LocalDateTime generatedAt) {
        this.id = id;
        this.insightText = insightText;
        this.generatedAt = generatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInsightText() {
        return insightText;
    }

    public void setInsightText(String insightText) {
        this.insightText = insightText;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
