package com.devkursat.wordifybe.dto;

import java.util.List;

public record CreateReadingResponse(
        List<String> source_words,
        ReadingContentResponse reading
) {
}
