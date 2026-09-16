package com.hongren.careerassistant.ai;

import com.hongren.careerassistant.ai.dto.ExtractFactsResponse;

public interface AiServiceClient {

    ExtractFactsResponse extractFacts(String text);
}
