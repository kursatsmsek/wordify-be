package com.devkursat.wordifybe.dto;

public record QuizWrongWordResponse(
        Long wordId,
        String english,
        String turkish,
        String userAnswer
) {
}
