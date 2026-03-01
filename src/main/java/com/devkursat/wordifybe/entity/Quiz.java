package com.devkursat.wordifybe.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_type", nullable = false, length = 20)
    private QuizType quizType;

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_direction", nullable = false, length = 20)
    private QuizDirection quizDirection = QuizDirection.TR_TO_EN;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "wrong_count", nullable = false)
    private Integer wrongCount;

    @Column(name = "score_percent", nullable = false)
    private Integer scorePercent;

    @Column(name = "quiz_date", nullable = false)
    private Instant quizDate;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizWrongWord> wrongWords = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (quizDate == null) {
            quizDate = Instant.now();
        }
    }

    public void addWrongWord(QuizWrongWord wrongWord) {
        wrongWords.add(wrongWord);
        wrongWord.setQuiz(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Integer scorePercent) {
        this.scorePercent = scorePercent;
    }

    public Instant getQuizDate() {
        return quizDate;
    }

    public void setQuizDate(Instant quizDate) {
        this.quizDate = quizDate;
    }

    public List<QuizWrongWord> getWrongWords() {
        return wrongWords;
    }

    public void setWrongWords(List<QuizWrongWord> wrongWords) {
        this.wrongWords = wrongWords;
    }
}
