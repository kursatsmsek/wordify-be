package com.devkursat.wordifybe.repository;

import com.devkursat.wordifybe.entity.Level;
import com.devkursat.wordifybe.entity.Word;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long>, JpaSpecificationExecutor<Word> {

    List<Word> findByLastWrongAtIsNotNullOrderByLastWrongAtDesc(Pageable pageable);

    List<Word> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM words ORDER BY random() LIMIT :count", nativeQuery = true)
    List<Word> findRandom(@org.springframework.data.repository.query.Param("count") int count);
}
