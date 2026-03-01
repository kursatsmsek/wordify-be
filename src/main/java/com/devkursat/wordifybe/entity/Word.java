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
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String english;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String turkish;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Level level;

    @Column(name = "success_rate", nullable = false)
    private Integer successRate = 0;

    @Column(name = "quiz_count", nullable = false)
    private Integer quizCount = 0;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_asked_at")
    private Instant lastAskedAt;

    @Column(name = "last_wrong_at")
    private Instant lastWrongAt;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WordExample> examples = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void addExample(WordExample example) {
        examples.add(example);
        example.setWord(this);
    }

    public void clearExamples() {
        examples.clear();
    }

    public void incrementQuizStats(boolean correct, Instant askedAt) {
        this.quizCount = this.quizCount + 1;
        if (correct) {
            this.correctCount = this.correctCount + 1;
        }
        this.lastAskedAt = askedAt;
        recalculateSuccessRate();
    }

    public void markWrong(Instant at) {
        this.lastWrongAt = at;
    }

    public void recalculateSuccessRate() {
        if (quizCount == null || quizCount == 0) {
            this.successRate = 0;
            return;
        }
        this.successRate = (int) Math.round((double) correctCount / (double) quizCount * 100.0);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getTurkish() {
        return turkish;
    }

    public void setTurkish(String turkish) {
        this.turkish = turkish;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Integer getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Integer successRate) {
        this.successRate = successRate;
    }

    public Integer getQuizCount() {
        return quizCount;
    }

    public void setQuizCount(Integer quizCount) {
        this.quizCount = quizCount;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastAskedAt() {
        return lastAskedAt;
    }

    public void setLastAskedAt(Instant lastAskedAt) {
        this.lastAskedAt = lastAskedAt;
    }

    public Instant getLastWrongAt() {
        return lastWrongAt;
    }

    public void setLastWrongAt(Instant lastWrongAt) {
        this.lastWrongAt = lastWrongAt;
    }

    public List<WordExample> getExamples() {
        return examples;
    }

    public void setExamples(List<WordExample> examples) {
        this.examples = examples;
    }
}
