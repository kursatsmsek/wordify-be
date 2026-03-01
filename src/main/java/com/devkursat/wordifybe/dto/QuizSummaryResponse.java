package com.devkursat.wordifybe.dto;

import com.devkursat.wordifybe.entity.QuizDirection;
import com.devkursat.wordifybe.entity.QuizType;

import java.time.Instant;

public record QuizSummaryResponse(
        Long id,
        QuizType quizType,
        QuizDirection quizDirection,
        Integer totalCount,
        Integer correctCount,
        Integer wrongCount,
        Integer scorePercent,
        Instant quizDate
) {
}
