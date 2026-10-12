package com.hongren.careerassistant.fact;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongren.careerassistant.fact.dto.FactResponse;

@RestController
@RequestMapping("/api/documents/{documentId}/facts")
public class CandidateFactController {

    private final CandidateFactService factService;

    public CandidateFactController(CandidateFactService factService) {
        this.factService = factService;
    }

    @PostMapping("/extract")
    public List<FactResponse> extractFacts(
            @PathVariable("documentId") UUID documentId) {

        return factService.extractFacts(documentId).stream()
                .map(FactResponse::from)
                .toList();
    }

    @GetMapping
    public List<FactResponse> getFacts(
            @PathVariable("documentId") UUID documentId) {

        return factService.getFactsByDocumentId(documentId).stream()
                .map(FactResponse::from)
                .toList();
    }
}