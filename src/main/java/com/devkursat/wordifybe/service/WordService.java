package com.devkursat.wordifybe.service;

import com.devkursat.wordifybe.dto.WordExampleResponse;
import com.devkursat.wordifybe.dto.WordRequest;
import com.devkursat.wordifybe.dto.WordResponse;
import com.devkursat.wordifybe.entity.Level;
import com.devkursat.wordifybe.entity.QuizType;
import com.devkursat.wordifybe.entity.Word;
import com.devkursat.wordifybe.entity.WordExample;
import com.devkursat.wordifybe.exception.ResourceNotFoundException;
import com.devkursat.wordifybe.repository.WordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class WordService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int DEFAULT_QUIZ_COUNT = 10;
    private static final int MAX_QUIZ_COUNT = 50;

    private static final Map<String, String> SORT_MAPPING = Map.of(
            "created_at", "createdAt",
            "success_rate", "successRate",
            "english", "english"
    );

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @Transactional(readOnly = true)
    public Page<WordResponse> getWords(String q, Level level, Integer page, Integer size, String sortField) {
        Pageable pageable = PageRequest.of(
                page == null ? DEFAULT_PAGE : Math.max(0, page),
                size == null ? DEFAULT_SIZE : Math.max(1, size),
                Sort.by(Sort.Direction.DESC, resolveSortField(sortField))
        );

        String normalizedQuery = normalize(q);
        return wordRepository.search(normalizedQuery, level, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public WordResponse getWordById(Long id) {
        Word word = findById(id);
        return toResponse(word);
    }

    @Transactional
    public WordResponse createWord(WordRequest request) {
        Word word = new Word();
        applyWordRequest(word, request);
        return toResponse(wordRepository.save(word));
    }

    @Transactional
    public WordResponse updateWord(Long id, WordRequest request) {
        Word word = findById(id);
        applyWordRequest(word, request);
        return toResponse(wordRepository.save(word));
    }

    @Transactional
    public void deleteWord(Long id) {
        Word word = findById(id);
        wordRepository.delete(word);
    }

    @Transactional(readOnly = true)
    public List<WordResponse> getQuizWords(QuizType type, Integer requestedCount) {
        int count = sanitizeCount(requestedCount);
        QuizType quizType = type == null ? QuizType.RANDOM : type;

        List<Word> words = switch (quizType) {
            case LAST_WRONG -> {
                List<Word> wrongWords = wordRepository.findByLastWrongAtIsNotNullOrderByLastWrongAtDesc(PageRequest.of(0, count));
                yield wrongWords.isEmpty() ? wordRepository.findRandom(count) : wrongWords;
            }
            case RECENT -> wordRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, count));
            case RANDOM -> wordRepository.findRandom(count);
        };

        return words.stream().map(this::toResponse).toList();
    }

    private String resolveSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "createdAt";
        }
        String mapped = SORT_MAPPING.get(sortField);
        if (mapped == null) {
            throw new IllegalArgumentException("Unsupported sort field: " + sortField);
        }
        return mapped;
    }

    private int sanitizeCount(Integer requestedCount) {
        int count = requestedCount == null ? DEFAULT_QUIZ_COUNT : requestedCount;
        if (count < 1) {
            throw new IllegalArgumentException("count must be greater than 0");
        }
        if (count > MAX_QUIZ_COUNT) {
            throw new IllegalArgumentException("count cannot be greater than 50");
        }
        return count;
    }

    private String normalize(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim();
    }

    private Word findById(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Word not found: " + id));
    }

    private void applyWordRequest(Word word, WordRequest request) {
        word.setEnglish(request.getEnglish().trim());
        word.setTurkish(request.getTurkish().trim());
        word.setLevel(request.getLevel());

        word.clearExamples();
        if (request.getExamples() != null) {
            request.getExamples().stream()
                    .filter(sentence -> sentence != null && !sentence.isBlank())
                    .map(String::trim)
                    .forEach(sentence -> {
                        WordExample example = new WordExample();
                        example.setSentence(sentence);
                        word.addExample(example);
                    });
        }
    }

    private WordResponse toResponse(Word word) {
        List<WordExampleResponse> examples = word.getExamples().stream()
                .map(example -> new WordExampleResponse(example.getId(), example.getSentence()))
                .toList();

        return new WordResponse(
                word.getId(),
                word.getEnglish(),
                word.getTurkish(),
                word.getLevel(),
                examples,
                word.getSuccessRate(),
                word.getQuizCount(),
                word.getCorrectCount(),
                word.getCreatedAt(),
                word.getLastAskedAt(),
                word.getLastWrongAt()
        );
    }
}
