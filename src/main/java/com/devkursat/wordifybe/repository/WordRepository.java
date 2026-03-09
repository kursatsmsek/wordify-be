package com.devkursat.wordifybe.repository;

import com.devkursat.wordifybe.entity.Word;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long>, JpaSpecificationExecutor<Word> {

    List<Word> findByLastWrongAtIsNotNullOrderByLastWrongAtDesc(Pageable pageable);

    List<Word> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT w FROM Word w ORDER BY function('RANDOM')")
    List<Word> findRandom(Pageable pageable);

    @Query("SELECT w.english FROM Word w ORDER BY function('RANDOM')")
    List<String> findRandomWords(Pageable pageable);
}
