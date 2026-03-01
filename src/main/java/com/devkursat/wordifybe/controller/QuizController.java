package com.devkursat.wordifybe.controller;

import com.devkursat.wordifybe.dto.QuizRequest;
import com.devkursat.wordifybe.dto.QuizResponse;
import com.devkursat.wordifybe.dto.QuizSummaryResponse;
import com.devkursat.wordifybe.dto.StatsResponse;
import com.devkursat.wordifybe.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public QuizResponse createQuiz(@Valid @RequestBody QuizRequest request) {
        return quizService.createQuiz(request);
    }

    @GetMapping
    public Page<QuizSummaryResponse> getQuizzes(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return quizService.getQuizzes(page, size);
    }

    @GetMapping("/{id}")
    public QuizResponse getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id);
    }

    @GetMapping("/stats")
    public StatsResponse getStats() {
        return quizService.getStats();
    }
}
