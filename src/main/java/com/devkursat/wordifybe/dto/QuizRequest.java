package com.devkursat.wordifybe.dto;

import com.devkursat.wordifybe.entity.QuizDirection;
import com.devkursat.wordifybe.entity.QuizType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class QuizRequest {

    @NotNull
    private QuizType quizType;

    @NotNull
    private QuizDirection quizDirection = QuizDirection.TR_TO_EN;

    @NotNull
    @Min(1)
    private Integer totalCount;

    @NotNull
    @Min(0)
    private Integer correctCount;

    @NotNull
    @Min(0)
    private Integer wrongCount;

    @Valid
    private List<QuizWrongWordRequest> wrongWords;

    private List<Long> askedWordIds;

    public QuizType getQuizType() {
        return quizType;
    }

    public void setQuizType(QuizType quizType) {
        this.quizType = quizType;
    }

    public QuizDirection getQuizDirection() {
        return quizDirection;
    }

    public void setQuizDirection(QuizDirection quizDirection) {
        this.quizDirection = quizDirection;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public List<QuizWrongWordRequest> getWrongWords() {
        return wrongWords;
    }

    public void setWrongWords(List<QuizWrongWordRequest> wrongWords) {
        this.wrongWords = wrongWords;
    }

    public List<Long> getAskedWordIds() {
        return askedWordIds;
    }

    public void setAskedWordIds(List<Long> askedWordIds) {
        this.askedWordIds = askedWordIds;
    }
}
