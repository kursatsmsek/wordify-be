package com.devkursat.wordifybe.dto;

import java.time.Instant;
import java.util.List;

public record ReadingDetailResponse(
        Long id,
        Instant created_at,
        List<String> source_words,
        ReadingContentResponse reading
) {
}
