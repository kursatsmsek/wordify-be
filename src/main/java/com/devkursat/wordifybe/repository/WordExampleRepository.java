package com.devkursat.wordifybe.repository;

import com.devkursat.wordifybe.entity.WordExample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordExampleRepository extends JpaRepository<WordExample, Long> {
}
