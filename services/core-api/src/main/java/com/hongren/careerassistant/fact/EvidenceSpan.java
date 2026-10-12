package com.hongren.careerassistant.fact;

public record EvidenceSpan(
        String sourceText,
        int start,
        int end) {
}