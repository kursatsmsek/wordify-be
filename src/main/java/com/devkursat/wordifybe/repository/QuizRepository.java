package com.devkursat.wordifybe.repository;

import com.devkursat.wordifybe.entity.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = {"wrongWords", "wrongWords.word"})
    Optional<Quiz> findDetailedById(Long id);

    @Query("SELECT COALESCE(AVG(q.scorePercent), 0) FROM Quiz q")
    Double getAverageScore();

    @Query("SELECT COALESCE(SUM(q.totalCount), 0) FROM Quiz q")
    Long getTotalQuestionCount();

    @Query("SELECT COALESCE(SUM(q.correctCount), 0) FROM Quiz q")
    Long getTotalCorrectCount();
}
