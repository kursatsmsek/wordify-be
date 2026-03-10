package com.devkursat.wordifybe.controller;

import com.devkursat.wordifybe.dto.CreateReadingRequest;
import com.devkursat.wordifybe.dto.CreateReadingResponse;
import com.devkursat.wordifybe.dto.ReadingDetailResponse;
import com.devkursat.wordifybe.dto.ReadingSummaryResponse;
import com.devkursat.wordifybe.service.ReadingService;
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
@RequestMapping("/api/readings")
public class ReadingController {

    private final ReadingService readingService;

    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @PostMapping("/create")
    public CreateReadingResponse createReading(@Valid @RequestBody(required = false) CreateReadingRequest request) {
        return readingService.createReading(request);
    }

    @GetMapping
    public Page<ReadingSummaryResponse> getReadings(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return readingService.getReadings(page, size);
    }

    @GetMapping("/{id}")
    public ReadingDetailResponse getReadingById(@PathVariable Long id) {
        return readingService.getReadingById(id);
    }
}
