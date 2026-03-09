package com.devkursat.wordifybe.controller;

import com.devkursat.wordifybe.dto.WordRequest;
import com.devkursat.wordifybe.dto.WordResponse;
import com.devkursat.wordifybe.entity.Level;
import com.devkursat.wordifybe.entity.QuizType;
import com.devkursat.wordifybe.service.WordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping
    public Page<WordResponse> getWords(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Level level,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "created_at") String sort
    ) {
        return wordService.getWords(q, level, page, size, sort);
    }

    @GetMapping("/{id}")
    public WordResponse getWordById(@PathVariable Long id) {
        return wordService.getWordById(id);
    }

    @PostMapping
    public WordResponse createWord(@Valid @RequestBody WordRequest request) {
        return wordService.createWord(request);
    }

    @PutMapping("/{id}")
    public WordResponse updateWord(@PathVariable Long id, @Valid @RequestBody WordRequest request) {
        return wordService.updateWord(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWord(@PathVariable Long id) {
        wordService.deleteWord(id);
    }

    @GetMapping("/quiz")
    public List<WordResponse> getQuizWords(
            @RequestParam QuizType type,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer count
    ) {
        return wordService.getQuizWords(type, count);
    }

    @GetMapping("/random")
    public List<String> getRandomWords(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer count
    ) {
        return wordService.getRandomWords(count);
    }
}
