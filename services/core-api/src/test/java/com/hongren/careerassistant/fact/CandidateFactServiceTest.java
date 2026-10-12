package com.hongren.careerassistant.fact;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hongren.careerassistant.ai.AiServiceClient;
import com.hongren.careerassistant.ai.AiServiceClientException;
import com.hongren.careerassistant.ai.dto.CandidateFactResponse;
import com.hongren.careerassistant.ai.dto.EvidenceSpanResponse;
import com.hongren.careerassistant.ai.dto.ExtractFactsResponse;
import com.hongren.careerassistant.document.CandidateDocument;
import com.hongren.careerassistant.document.DocumentNotFoundException;
import com.hongren.careerassistant.document.DocumentService;
import com.hongren.careerassistant.document.DocumentType;
import com.hongren.careerassistant.document.dto.CreateDocumentRequest;

class CandidateFactServiceTest {

    @Test
    void extractsAndStoresPendingReviewFactsforDocument() {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        String sourceText = "Java backend experience";
        String originalText = "\n  " + sourceText + "\nPython course project";

        CandidateDocument document = documentService.createDocument(
                new CreateDocumentRequest(
                        "My Resume",
                        DocumentType.RESUME,
                        originalText));

        int start = originalText.indexOf(sourceText);
        int end = start + sourceText.length();

        CandidateFactResponse aiFact = new CandidateFactResponse(
                "experience",
                sourceText,
                new EvidenceSpanResponse(sourceText, start, end),
                0.5,
                "pending_review");

        // 只模拟 AI 边界，文档读取、事实转换和保存
        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(new ExtractFactsResponse(
                        "mock",
                        List.of(aiFact)));

        List<CandidateFact> facts
                = factService.extractFacts(document.id());

        assertThat(facts).hasSize(1);

        CandidateFact fact = facts.getFirst();

        assertThat(fact.id()).isNotNull();
        assertThat(fact.documentId()).isEqualTo(document.id());
        assertThat(fact.factType()).isEqualTo(FactType.EXPERIENCE);
        assertThat(fact.value()).isEqualTo(sourceText);
        assertThat(fact.evidence())
                .isEqualTo(new EvidenceSpan(sourceText, start, end));
        assertThat(fact.confidence()).isEqualTo(0.5);
        assertThat(fact.status()).isEqualTo(ReviewStatus.PENDING_REVIEW);
        assertThat(fact.createdAt()).isNotNull();

        // 查询必须返回刚才保存的事实，包括同一个事实 ID。
        assertThat(factService.getFactsByDocumentId(document.id()))
                .containsExactly(fact);

        verify(aiServiceClient).extractFacts(originalText);
    }

    @Test
    void extractFactsDoesNotCallAiWhenDocumentIsMissing() {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        UUID missingDocumentId = UUID.randomUUID();

        assertThatThrownBy(()
                -> factService.extractFacts(missingDocumentId))
                .isInstanceOf(DocumentNotFoundException.class);

        // 文档读取失败后，不应继续发起 AI 调用。
        verifyNoInteractions(aiServiceClient);
    }

    @Test
    void rejectsEvidenceThatDoesNotMatchOriginalText() {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        String originalText = "Java backend experience";

        CandidateDocument document = documentService.createDocument(
                new CreateDocumentRequest(
                        "My Resume",
                        DocumentType.RESUME,
                        originalText));

        // 故意将 start 从 0 改成 1，使证据位置与 sourceText 不一致。
        CandidateFactResponse invalidFact = new CandidateFactResponse(
                "experience",
                originalText,
                new EvidenceSpanResponse(
                        originalText,
                        1,
                        originalText.length()),
                0.5,
                "pending_review");

        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(new ExtractFactsResponse(
                        "mock",
                        List.of(invalidFact)));

        assertThatThrownBy(()
                -> factService.extractFacts(document.id()))
                .isInstanceOf(AiServiceClientException.class)
                .hasMessage("AI service returned invalid evidence");

        assertThat(factService.getFactsByDocumentId(document.id()))
                .isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "-1, 4",
        "0, 24",
        "2, 2",
        "5, 3"
    })
    void rejectsInvalidEvidenceRange(int start, int end) {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        String originalText = "Java backend experience";

        CandidateDocument document = documentService.createDocument(
                new CreateDocumentRequest(
                        "My Resume",
                        DocumentType.RESUME,
                        originalText));

        CandidateFactResponse invalidFact = new CandidateFactResponse(
                "experience",
                originalText,
                new EvidenceSpanResponse(originalText, start, end),
                0.5,
                "pending_review");

        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(new ExtractFactsResponse(
                        "mock",
                        List.of(invalidFact)));

        assertThatThrownBy(()
                -> factService.extractFacts(document.id()))
                .isInstanceOf(AiServiceClientException.class)
                .hasMessage("AI service returned invalid evidence");

        assertThat(factService.getFactsByDocumentId(document.id()))
                .isEmpty();
    }

    @Test
    void acceptsEvidenceUsingPythonOffsetsWhenTextContainsEmoji() {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        String sourceText = "Java backend experience";
        String originalText = "😀\n" + sourceText;

        CandidateDocument document = documentService.createDocument(
                new CreateDocumentRequest(
                        "My Resume",
                        DocumentType.RESUME,
                        originalText));

        // Python 中 emoji 和换行各占一个码点，因此证据从位置 2 开始。
        CandidateFactResponse aiFact = new CandidateFactResponse(
                "experience",
                sourceText,
                new EvidenceSpanResponse(sourceText, 2, 25),
                0.5,
                "pending_review");

        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(new ExtractFactsResponse(
                        "mock",
                        List.of(aiFact)));

        List<CandidateFact> facts
                = factService.extractFacts(document.id());

        assertThat(facts).hasSize(1);

        CandidateFact fact = facts.getFirst();

        assertThat(fact.value()).isEqualTo(sourceText);
        assertThat(fact.evidence())
                .isEqualTo(new EvidenceSpan(sourceText, 2, 25));

        assertThat(factService.getFactsByDocumentId(document.id()))
                .containsExactly(fact);
    }

    @Test
    void rejectsEntireBatchWhenOneFactHasInvalidEvidence() {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        String firstSource = "Java backend experience";
        String secondSource = "Python course project";
        String originalText = firstSource + "\n" + secondSource;

        CandidateDocument document = documentService.createDocument(
                new CreateDocumentRequest(
                        "My Resume",
                        DocumentType.RESUME,
                        originalText));

        CandidateFactResponse validFact = new CandidateFactResponse(
                "experience",
                firstSource,
                new EvidenceSpanResponse(
                        firstSource,
                        0,
                        firstSource.length()),
                0.5,
                "pending_review");

        int secondStart = originalText.indexOf(secondSource);

        // 第二条故意错位；第一条有效，测试是否发生部分保存。
        CandidateFactResponse invalidFact = new CandidateFactResponse(
                "project",
                secondSource,
                new EvidenceSpanResponse(
                        secondSource,
                        secondStart + 1,
                        secondStart + secondSource.length()),
                0.5,
                "pending_review");

        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(new ExtractFactsResponse(
                        "mock",
                        List.of(validFact, invalidFact)));

        assertThatThrownBy(()
                -> factService.extractFacts(document.id()))
                .isInstanceOf(AiServiceClientException.class)
                .hasMessage("AI service returned invalid evidence");

        assertThat(factService.getFactsByDocumentId(document.id()))
                .isEmpty();
    }

    @Test
    void preservesExistingFactsWhenNewExtractionFailsValidation() {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        String originalText = "Java backend experience";

        CandidateDocument document = documentService.createDocument(
                new CreateDocumentRequest(
                        "My Resume",
                        DocumentType.RESUME,
                        originalText));

        CandidateFactResponse validFact = new CandidateFactResponse(
                "experience",
                originalText,
                new EvidenceSpanResponse(
                        originalText, 0, originalText.length()),
                0.5,
                "pending_review");

        CandidateFactResponse invalidFact = new CandidateFactResponse(
                "experience",
                originalText,
                new EvidenceSpanResponse(
                        originalText, 1, originalText.length()),
                0.5,
                "pending_review");

        // 同一调用第一次返回有效结果，第二次返回无效证据。
        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(
                        new ExtractFactsResponse(
                                "mock", List.of(validFact)),
                        new ExtractFactsResponse(
                                "mock", List.of(invalidFact)));

        List<CandidateFact> originalFacts
                = factService.extractFacts(document.id());

        assertThat(originalFacts).hasSize(1);

        assertThatThrownBy(()
                -> factService.extractFacts(document.id()))
                .isInstanceOf(AiServiceClientException.class)
                .hasMessage("AI service returned invalid evidence");

        // 失败的新结果不能覆盖已有事实。
        assertThat(factService.getFactsByDocumentId(document.id()))
                .containsExactlyElementsOf(originalFacts);
    }

    @Test
    void initializesPendingReviewEvenWhenAiReturnsConfirmed() {
        DocumentService documentService = new DocumentService();
        AiServiceClient aiServiceClient = mock(AiServiceClient.class);

        CandidateFactService factService
                = new CandidateFactService(documentService, aiServiceClient);

        String originalText = "Java backend experience";

        CandidateDocument document = documentService.createDocument(
                new CreateDocumentRequest(
                        "My Resume",
                        DocumentType.RESUME,
                        originalText));

        // 模拟 AI 越过业务边界，直接返回已确认状态。
        CandidateFactResponse aiFact = new CandidateFactResponse(
                "experience",
                originalText,
                new EvidenceSpanResponse(
                        originalText, 0, originalText.length()),
                0.5,
                "confirmed");

        when(aiServiceClient.extractFacts(originalText))
                .thenReturn(new ExtractFactsResponse(
                        "mock", List.of(aiFact)));

        List<CandidateFact> facts
                = factService.extractFacts(document.id());

        assertThat(facts).hasSize(1);
        assertThat(facts.getFirst().status())
                .isEqualTo(ReviewStatus.PENDING_REVIEW);

        assertThat(factService.getFactsByDocumentId(document.id()))
                .containsExactlyElementsOf(facts);
    }
}
