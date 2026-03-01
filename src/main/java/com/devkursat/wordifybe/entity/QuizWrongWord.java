package com.devkursat.wordifybe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_wrong_words")
public class QuizWrongWord {

    @EmbeddedId
    private QuizWrongWordId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("quizId")
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("wordId")
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    public QuizWrongWordId getId() {
        return id;
    }

    public void setId(QuizWrongWordId id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
}
