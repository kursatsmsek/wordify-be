package com.devkursat.wordifybe.dto;

public record StatsResponse(
        Long totalWords,
        Long totalQuizzes,
        Integer averageScore,
        Long totalQuestionsAnswered,
        Integer overallSuccessRate
) {
}
