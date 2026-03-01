package com.devkursat.wordifybe.dto;

import com.devkursat.wordifybe.entity.Level;

import java.time.Instant;
import java.util.List;

public record WordResponse(
        Long id,
        String english,
        String turkish,
        Level level,
        List<WordExampleResponse> examples,
        Integer successRate,
        Integer quizCount,
        Integer correctCount,
        Instant createdAt,
        Instant lastAskedAt,
        Instant lastWrongAt
) {
}
