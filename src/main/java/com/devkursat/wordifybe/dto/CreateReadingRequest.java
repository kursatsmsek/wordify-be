package com.devkursat.wordifybe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CreateReadingRequest(
        @Min(1) @Max(100) Integer count,
        @Size(max = 1000) String instruction
) {
}
