package com.hongren.careerassistant.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EvidenceSpanResponse(
        @JsonProperty("source_text") String sourceText,
        int start,
        int end
) {
}