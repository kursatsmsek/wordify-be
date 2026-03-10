package com.devkursat.wordifybe.dto;

import java.time.Instant;
import java.util.List;

public record ReadingSummaryResponse(
        Long id,
        Instant created_at,
        String title,
        List<String> source_words
) {
}
