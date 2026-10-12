package com.hongren.careerassistant.fact;

import java.time.Instant;
import java.util.UUID;

public record CandidateFact(
        UUID id,
        UUID documentId,
        FactType factType,
        String value,
        EvidenceSpan evidence,
        double confidence,
        ReviewStatus status,
        Instant createdAt) {
}