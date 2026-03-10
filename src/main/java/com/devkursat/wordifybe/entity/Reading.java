package com.devkursat.wordifybe.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "readings")
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passage_en", nullable = false, columnDefinition = "TEXT")
    private String passageEn;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "passage_tr", nullable = false, columnDefinition = "TEXT")
    private String passageTr;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "reading", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingSourceWord> sourceWords = new ArrayList<>();

    @OneToMany(mappedBy = "reading", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingVocabularyWord> vocabularyWords = new ArrayList<>();

    @OneToMany(mappedBy = "reading", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReadingQuestionEntity> questions = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void addSourceWord(ReadingSourceWord sourceWord) {
        sourceWords.add(sourceWord);
        sourceWord.setReading(this);
    }

    public void addVocabularyWord(ReadingVocabularyWord vocabularyWord) {
        vocabularyWords.add(vocabularyWord);
        vocabularyWord.setReading(this);
    }

    public void addQuestion(ReadingQuestionEntity question) {
        questions.add(question);
        question.setReading(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassageEn() {
        return passageEn;
    }

    public void setPassageEn(String passageEn) {
        this.passageEn = passageEn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassageTr() {
        return passageTr;
    }

    public void setPassageTr(String passageTr) {
        this.passageTr = passageTr;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<ReadingSourceWord> getSourceWords() {
        return sourceWords;
    }

    public void setSourceWords(List<ReadingSourceWord> sourceWords) {
        this.sourceWords = sourceWords;
    }

    public List<ReadingVocabularyWord> getVocabularyWords() {
        return vocabularyWords;
    }

    public void setVocabularyWords(List<ReadingVocabularyWord> vocabularyWords) {
        this.vocabularyWords = vocabularyWords;
    }

    public List<ReadingQuestionEntity> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ReadingQuestionEntity> questions) {
        this.questions = questions;
    }
}
