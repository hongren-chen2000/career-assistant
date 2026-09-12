package com.hongren.careerassistant.document;

import java.time.Instant;
import java.util.UUID;

public record CandidateDocument(
        UUID id,
        String title,
        DocumentType type,
        String content,
        Instant createdAt
        ) {

}
