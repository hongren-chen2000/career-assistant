package com.hongren.careerassistant.document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.hongren.careerassistant.document.dto.CreateDocumentRequest;

@Service
public class DocumentService {

    private final Map<UUID, CandidateDocument> documents = new ConcurrentHashMap<>();

    public CandidateDocument createDocument(CreateDocumentRequest request) {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();

        CandidateDocument document = new CandidateDocument(
                id,
                request.title(),
                request.type(),
                request.content(),
                createdAt
        );

        documents.put(document.id(), document);
        return document;
    }

    public CandidateDocument getDocumentById(UUID id) {

        CandidateDocument document = documents.get(id);

        if (document == null) {
            throw new DocumentNotFoundException(id);
        }

        return document;
    }
}
