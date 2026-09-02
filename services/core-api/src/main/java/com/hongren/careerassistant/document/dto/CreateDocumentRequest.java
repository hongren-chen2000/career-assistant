package com.hongren.careerassistant.document.dto;

import com.hongren.careerassistant.document.DocumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDocumentRequest(
        @NotBlank(message = "title must not be blank")
        String title,

        @NotNull(message = "type must not be null")
        DocumentType type,

        @NotBlank(message = "content must not be blank")
        String content
) {
}