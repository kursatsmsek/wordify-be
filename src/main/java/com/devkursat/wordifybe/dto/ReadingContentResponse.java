package com.devkursat.wordifybe.dto;

import java.util.List;

public record ReadingContentResponse(
        String title,
        String passage_en,
        String passage_tr,
        List<ReadingWordItem> target_words,
        List<ReadingWordItem> extra_words,
        List<ReadingQuestion> questions
) {
}
