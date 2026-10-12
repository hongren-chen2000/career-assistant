package com.hongren.careerassistant.fact;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.hongren.careerassistant.ai.AiServiceClient;
import com.hongren.careerassistant.ai.AiServiceClientException;
import com.hongren.careerassistant.ai.dto.EvidenceSpanResponse;
import com.hongren.careerassistant.ai.dto.ExtractFactsResponse;
import com.hongren.careerassistant.document.CandidateDocument;
import com.hongren.careerassistant.document.DocumentService;

@Service
public class CandidateFactService {

    private final DocumentService documentService;
    private final AiServiceClient aiServiceClient;

    private final Map<UUID, List<CandidateFact>> factsByDocument
            = new ConcurrentHashMap<>();

    public CandidateFactService(
            DocumentService documentService,
            AiServiceClient aiServiceClient) {
        this.documentService = documentService;
        this.aiServiceClient = aiServiceClient;
    }

    public List<CandidateFact> extractFacts(UUID documentId) {
        CandidateDocument document
                = documentService.getDocumentById(documentId);

        ExtractFactsResponse response
                = aiServiceClient.extractFacts(document.content());

        List<CandidateFact> facts = response.facts().stream()
                .map(aiFact -> {
                    validateEvidence(document.content(), aiFact.evidence());

                    return new CandidateFact(
                            UUID.randomUUID(),
                            document.id(),
                            FactType.valueOf(
                                    aiFact.factType().toUpperCase(Locale.ROOT)),
                            aiFact.value(),
                            new EvidenceSpan(
                                    aiFact.evidence().sourceText(),
                                    aiFact.evidence().start(),
                                    aiFact.evidence().end()),
                            aiFact.confidence(),
                            ReviewStatus.PENDING_REVIEW,
                            Instant.now());
                })
                .toList();

        // 所有事实都转换、校验成功后，才更新存储。
        factsByDocument.put(documentId, facts);

        return facts;
    }

    public List<CandidateFact> getFactsByDocumentId(UUID documentId) {
        documentService.getDocumentById(documentId);

        return factsByDocument.getOrDefault(documentId, List.of());
    }

    private void validateEvidence(
            String originalText,
            EvidenceSpanResponse evidence) {

        if (evidence == null || evidence.sourceText() == null) {
            throw new AiServiceClientException(
                    "AI service returned invalid evidence");
        }

        int start = evidence.start();
        int end = evidence.end();

        // Python 字符串索引按 Unicode 码点计算，Java String 索引按 UTF-16 计算。
        int textLength = originalText.codePointCount(
                0, originalText.length());

        if (start < 0 || end <= start || end > textLength) {
            throw new AiServiceClientException(
                    "AI service returned invalid evidence");
        }

        int javaStart = originalText.offsetByCodePoints(0, start);
        int javaEnd = originalText.offsetByCodePoints(0, end);

        String sourceSlice = originalText.substring(javaStart, javaEnd);

        if (!sourceSlice.equals(evidence.sourceText())) {
            throw new AiServiceClientException(
                    "AI service returned invalid evidence");
        }
    }
}
