package com.devkursat.wordifybe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reading_vocabulary_words")
public class ReadingVocabularyWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reading_id", nullable = false)
    private Reading reading;

    @Enumerated(EnumType.STRING)
    @Column(name = "word_type", nullable = false, length = 10)
    private ReadingWordType wordType;

    @Column(name = "position_index", nullable = false)
    private Integer positionIndex;

    @Column(nullable = false)
    private String word;

    @Column(name = "meaning_tr", columnDefinition = "TEXT")
    private String meaningTr;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reading getReading() {
        return reading;
    }

    public void setReading(Reading reading) {
        this.reading = reading;
    }

    public ReadingWordType getWordType() {
        return wordType;
    }

    public void setWordType(ReadingWordType wordType) {
        this.wordType = wordType;
    }

    public Integer getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(Integer positionIndex) {
        this.positionIndex = positionIndex;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaningTr() {
        return meaningTr;
    }

    public void setMeaningTr(String meaningTr) {
        this.meaningTr = meaningTr;
    }
}
