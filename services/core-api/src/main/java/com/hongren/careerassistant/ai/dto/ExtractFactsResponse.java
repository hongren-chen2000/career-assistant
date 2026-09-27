package com.hongren.careerassistant.ai.dto;

import java.util.List;

public record ExtractFactsResponse(
        String mode,
        List<CandidateFactResponse> facts
) {
}
