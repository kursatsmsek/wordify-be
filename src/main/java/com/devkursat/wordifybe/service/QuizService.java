package com.devkursat.wordifybe.service;

import com.devkursat.wordifybe.dto.QuizRequest;
import com.devkursat.wordifybe.dto.QuizResponse;
import com.devkursat.wordifybe.dto.QuizSummaryResponse;
import com.devkursat.wordifybe.dto.QuizWrongWordRequest;
import com.devkursat.wordifybe.dto.QuizWrongWordResponse;
import com.devkursat.wordifybe.dto.StatsResponse;
import com.devkursat.wordifybe.entity.Quiz;
import com.devkursat.wordifybe.entity.QuizWrongWord;
import com.devkursat.wordifybe.entity.QuizWrongWordId;
import com.devkursat.wordifybe.entity.Word;
import com.devkursat.wordifybe.exception.ResourceNotFoundException;
import com.devkursat.wordifybe.repository.QuizRepository;
import com.devkursat.wordifybe.repository.WordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final WordRepository wordRepository;

    public QuizService(QuizRepository quizRepository, WordRepository wordRepository) {
        this.quizRepository = quizRepository;
        this.wordRepository = wordRepository;
    }

    @Transactional
    public QuizResponse createQuiz(QuizRequest request) {
        validateRequest(request);

        Quiz quiz = new Quiz();
        quiz.setQuizType(request.getQuizType());
        quiz.setQuizDirection(request.getQuizDirection());
        quiz.setTotalCount(request.getTotalCount());
        quiz.setCorrectCount(request.getCorrectCount());
        quiz.setWrongCount(request.getWrongCount());
        quiz.setScorePercent(calculateScore(request.getCorrectCount(), request.getTotalCount()));

        Quiz savedQuiz = quizRepository.save(quiz);

        List<QuizWrongWordRequest> wrongWordRequests =
                request.getWrongWords() == null ? List.of() : request.getWrongWords();

        Map<Long, Word> wordMap = loadWordMap(wrongWordRequests.stream().map(QuizWrongWordRequest::getWordId).toList());

        for (QuizWrongWordRequest wrongWordRequest : wrongWordRequests) {
            Word word = wordMap.get(wrongWordRequest.getWordId());
            QuizWrongWord wrongWord = new QuizWrongWord();
            wrongWord.setId(new QuizWrongWordId(savedQuiz.getId(), word.getId()));
            wrongWord.setWord(word);
            wrongWord.setUserAnswer(wrongWordRequest.getUserAnswer());
            savedQuiz.addWrongWord(wrongWord);
        }

        updateWordStats(request, wordMap);

        Quiz finalQuiz = quizRepository.save(savedQuiz);
        return toResponse(finalQuiz);
    }

    @Transactional(readOnly = true)
    public Page<QuizSummaryResponse> getQuizzes(Integer page, Integer size) {
        int pageNo = page == null ? 0 : Math.max(0, page);
        int pageSize = size == null ? 20 : Math.max(1, size);

        return quizRepository.findAll(
                        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "quizDate"))
                )
                .map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Long id) {
        Quiz quiz = quizRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));
        return toResponse(quiz);
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        long totalWords = wordRepository.count();
        long totalQuizzes = quizRepository.count();
        int averageScore = (int) Math.round(quizRepository.getAverageScore());
        long totalQuestions = quizRepository.getTotalQuestionCount();
        long totalCorrect = quizRepository.getTotalCorrectCount();
        int overallSuccessRate = totalQuestions == 0 ? 0 : (int) Math.round((double) totalCorrect / (double) totalQuestions * 100.0);

        return new StatsResponse(totalWords, totalQuizzes, averageScore, totalQuestions, overallSuccessRate);
    }

    private void updateWordStats(QuizRequest request, Map<Long, Word> wrongWordMap) {
        Instant now = Instant.now();
        Set<Long> wrongWordIds = wrongWordMap.keySet();

        Set<Long> askedWordIds = new HashSet<>();
        if (request.getAskedWordIds() != null && !request.getAskedWordIds().isEmpty()) {
            askedWordIds.addAll(request.getAskedWordIds());
        } else {
            askedWordIds.addAll(wrongWordIds);
        }

        if (askedWordIds.isEmpty()) {
            return;
        }

        List<Word> askedWords = wordRepository.findAllById(askedWordIds);
        if (askedWords.size() != askedWordIds.size()) {
            throw new IllegalArgumentException("One or more askedWordIds are invalid");
        }

        for (Word word : askedWords) {
            boolean isWrong = wrongWordIds.contains(word.getId());
            word.incrementQuizStats(!isWrong, now);
            if (isWrong) {
                word.markWrong(now);
            }
        }

        wordRepository.saveAll(askedWords);
    }

    private Map<Long, Word> loadWordMap(List<Long> ids) {
        Map<Long, Word> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }

        List<Word> words = wordRepository.findAllById(ids);
        for (Word word : words) {
            map.put(word.getId(), word);
        }

        if (map.size() != new HashSet<>(ids).size()) {
            throw new IllegalArgumentException("One or more wrongWords.wordId values are invalid");
        }

        return map;
    }

    private void validateRequest(QuizRequest request) {
        if (request.getCorrectCount() + request.getWrongCount() != request.getTotalCount()) {
            throw new IllegalArgumentException("correctCount + wrongCount must be equal to totalCount");
        }

        int wrongWordsSize = request.getWrongWords() == null ? 0 : request.getWrongWords().size();
        if (wrongWordsSize != request.getWrongCount()) {
            throw new IllegalArgumentException("wrongWords size must match wrongCount");
        }
    }

    private int calculateScore(int correctCount, int totalCount) {
        return (int) Math.round((double) correctCount / (double) totalCount * 100.0);
    }

    private QuizResponse toResponse(Quiz quiz) {
        List<QuizWrongWordResponse> wrongWords = quiz.getWrongWords().stream()
                .map(wrongWord -> new QuizWrongWordResponse(
                        wrongWord.getWord().getId(),
                        wrongWord.getWord().getEnglish(),
                        wrongWord.getWord().getTurkish(),
                        wrongWord.getUserAnswer()
                ))
                .toList();

        return new QuizResponse(
                quiz.getId(),
                quiz.getQuizType(),
                quiz.getQuizDirection(),
                quiz.getTotalCount(),
                quiz.getCorrectCount(),
                quiz.getWrongCount(),
                quiz.getScorePercent(),
                quiz.getQuizDate(),
                wrongWords
        );
    }

    private QuizSummaryResponse toSummaryResponse(Quiz quiz) {
        return new QuizSummaryResponse(
                quiz.getId(),
                quiz.getQuizType(),
                quiz.getQuizDirection(),
                quiz.getTotalCount(),
                quiz.getCorrectCount(),
                quiz.getWrongCount(),
                quiz.getScorePercent(),
                quiz.getQuizDate()
        );
    }
}
