package com.devkursat.wordifybe.dto;

import com.devkursat.wordifybe.entity.Level;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class WordRequest {

    @NotBlank
    private String english;

    @NotBlank
    private String turkish;

    private Level level;

    private List<String> examples;

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

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }
}
