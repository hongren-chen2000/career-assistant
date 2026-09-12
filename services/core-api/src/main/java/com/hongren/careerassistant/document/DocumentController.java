package com.hongren.careerassistant.document;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hongren.careerassistant.document.dto.CreateDocumentRequest;
import com.hongren.careerassistant.document.dto.DocumentResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocument(
            @Valid @RequestBody CreateDocumentRequest request) {

        CandidateDocument document = documentService.createDocument(request);
        return DocumentResponse.from(document);
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocument(@PathVariable("id") UUID id) {
        CandidateDocument document = documentService.getDocumentById(id);
        return DocumentResponse.from(document);
    }
}
