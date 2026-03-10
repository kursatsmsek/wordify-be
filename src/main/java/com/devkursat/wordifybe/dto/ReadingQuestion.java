package com.devkursat.wordifybe.dto;

import java.util.List;

public record ReadingQuestion(
        String question,
        List<ReadingQuestionOption> options,
        String answer
) {
}
