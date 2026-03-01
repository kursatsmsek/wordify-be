package com.devkursat.wordifybe.repository;

import com.devkursat.wordifybe.entity.Level;
import com.devkursat.wordifybe.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {

    @Query("""
            SELECT w FROM Word w
            WHERE (:q IS NULL OR lower(w.english) LIKE lower(concat('%', :q, '%'))
                OR lower(w.turkish) LIKE lower(concat('%', :q, '%')))
              AND (:level IS NULL OR w.level = :level)
            """)
    Page<Word> search(@Param("q") String q, @Param("level") Level level, Pageable pageable);

    List<Word> findByLastWrongAtIsNotNullOrderByLastWrongAtDesc(Pageable pageable);

    List<Word> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query(value = "SELECT * FROM words ORDER BY random() LIMIT :count", nativeQuery = true)
    List<Word> findRandom(@Param("count") int count);
}
