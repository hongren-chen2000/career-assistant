package com.hongren.careerassistant.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CandidateFactResponse(
        @JsonProperty("fact_type") String factType,
        String value,
        EvidenceSpanResponse evidence,
        double confidence,
        String status
) {
}
