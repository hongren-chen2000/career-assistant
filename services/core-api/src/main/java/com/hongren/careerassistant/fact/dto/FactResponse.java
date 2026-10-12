package com.hongren.careerassistant.fact.dto;

import java.time.Instant;
import java.util.UUID;

import com.hongren.careerassistant.fact.CandidateFact;
import com.hongren.careerassistant.fact.EvidenceSpan;
import com.hongren.careerassistant.fact.FactType;
import com.hongren.careerassistant.fact.ReviewStatus;

public record FactResponse(
        UUID id,
        UUID documentId,
        FactType factType,
        String value,
        EvidenceSpan evidence,
        double confidence,
        ReviewStatus status,
        Instant createdAt) {

    public static FactResponse from(CandidateFact fact) {
        return new FactResponse(
                fact.id(),
                fact.documentId(),
                fact.factType(),
                fact.value(),
                fact.evidence(),
                fact.confidence(),
                fact.status(),
                fact.createdAt());
    }
}