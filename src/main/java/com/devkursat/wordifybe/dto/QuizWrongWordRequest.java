package com.devkursat.wordifybe.dto;

import jakarta.validation.constraints.NotNull;

public class QuizWrongWordRequest {

    @NotNull
    private Long wordId;

    private String userAnswer;

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
}
