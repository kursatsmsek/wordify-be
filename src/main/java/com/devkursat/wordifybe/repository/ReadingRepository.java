package com.devkursat.wordifybe.repository;

import com.devkursat.wordifybe.entity.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    default Optional<Reading> findDetailedById(Long id) {
        return findById(id);
    }
}
