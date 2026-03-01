package com.devkursat.wordifybe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuizWrongWordId implements Serializable {

    @Column(name = "quiz_id")
    private Long quizId;

    @Column(name = "word_id")
    private Long wordId;

    public QuizWrongWordId() {
    }

    public QuizWrongWordId(Long quizId, Long wordId) {
        this.quizId = quizId;
        this.wordId = wordId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuizWrongWordId that)) {
            return false;
        }
        return Objects.equals(quizId, that.quizId) && Objects.equals(wordId, that.wordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, wordId);
    }
}
