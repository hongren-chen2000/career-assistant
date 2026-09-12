package com.hongren.careerassistant.document.dto;

import java.time.Instant;
import java.util.UUID;

import com.hongren.careerassistant.document.CandidateDocument;
import com.hongren.careerassistant.document.DocumentType;

public record DocumentResponse(
        UUID id,
        String title,
        DocumentType type,
        String content,
        Instant createdAt
        ) {

    public static DocumentResponse from(CandidateDocument document) {
        return new DocumentResponse(
                document.id(),
                document.title(),
                document.type(),
                document.content(),
                document.createdAt()
        );
    }
}
