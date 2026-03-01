package com.devkursat.wordifybe.dto;

import com.devkursat.wordifybe.entity.QuizDirection;
import com.devkursat.wordifybe.entity.QuizType;

import java.time.Instant;
import java.util.List;

public record QuizResponse(
        Long id,
        QuizType quizType,
        QuizDirection quizDirection,
        Integer totalCount,
        Integer correctCount,
        Integer wrongCount,
        Integer scorePercent,
        Instant quizDate,
        List<QuizWrongWordResponse> wrongWords
) {
}
