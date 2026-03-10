package com.devkursat.wordifybe.service;

import com.devkursat.wordifybe.dto.CreateReadingRequest;
import com.devkursat.wordifybe.dto.CreateReadingResponse;
import com.devkursat.wordifybe.dto.ReadingContentResponse;
import com.devkursat.wordifybe.dto.ReadingQuestion;
import com.devkursat.wordifybe.dto.ReadingQuestionOption;
import com.devkursat.wordifybe.dto.ReadingDetailResponse;
import com.devkursat.wordifybe.dto.ReadingSummaryResponse;
import com.devkursat.wordifybe.dto.ReadingWordItem;
import com.devkursat.wordifybe.entity.Reading;
import com.devkursat.wordifybe.entity.ReadingQuestionEntity;
import com.devkursat.wordifybe.entity.ReadingQuestionOptionEntity;
import com.devkursat.wordifybe.entity.ReadingSourceWord;
import com.devkursat.wordifybe.entity.ReadingVocabularyWord;
import com.devkursat.wordifybe.entity.ReadingWordType;
import com.devkursat.wordifybe.exception.AiServiceException;
import com.devkursat.wordifybe.exception.ResourceNotFoundException;
import com.devkursat.wordifybe.repository.ReadingRepository;
import com.devkursat.wordifybe.repository.WordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReadingService {

    private static final int DEFAULT_WORD_COUNT = 15;
    private static final String SYSTEM_PROMPT = """
            You are a JSON API. You only output raw JSON objects. Never use markdown.
            Never use code blocks. No explanations. Your entire response must be a
            single valid JSON object and nothing else.
            """;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WordService wordService;
    private final ReadingRepository readingRepository;
    private final WordRepository wordRepository;
    private final RestClient restClient;
    private final String chatPath;
    private final String model;
    private final String think;
    private final String authToken;
    private final String cookie;

    public ReadingService(
            WordService wordService,
            ReadingRepository readingRepository,
            WordRepository wordRepository,
            @Value("${ollama.api.base-url}") String baseUrl,
            @Value("${ollama.api.chat-path}") String chatPath,
            @Value("${ollama.api.model}") String model,
            @Value("${ollama.api.think}") String think,
            @Value("${ollama.api.authorization-token}") String authToken,
            @Value("${ollama.api.cookie:}") String cookie
    ) {
        this.wordService = wordService;
        this.readingRepository = readingRepository;
        this.wordRepository = wordRepository;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.chatPath = chatPath;
        this.model = model;
        this.think = think;
        this.authToken = authToken;
        this.cookie = cookie;
    }

    public CreateReadingResponse createReading(CreateReadingRequest request) {
        int count = request == null || request.count() == null ? DEFAULT_WORD_COUNT : request.count();
        String instruction = request == null ? null : request.instruction();
        List<String> sourceWords = wordService.getRandomWords(count);

        if (sourceWords.isEmpty()) {
            throw new IllegalArgumentException("No words available to generate reading content");
        }
        if (authToken == null || authToken.isBlank()) {
            throw new AiServiceException("OLLAMA_API_AUTH_TOKEN is not configured");
        }

        Map<String, Object> payload = buildPayload(sourceWords, instruction);

        try {
            String rawResponse = restClient.post()
                    .uri(chatPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .headers(headers -> {
                        if (cookie != null && !cookie.isBlank()) {
                            headers.add(HttpHeaders.COOKIE, cookie);
                        }
                    })
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            ReadingContentResponse parsedReading = parseReading(rawResponse);
            ReadingContentResponse normalizedReading = normalizeReading(sourceWords, parsedReading);
            validateReading(normalizedReading);
            saveReading(sourceWords, normalizedReading);

            return new CreateReadingResponse(sourceWords, normalizedReading);
        } catch (RestClientException ex) {
            throw new AiServiceException("AI API request failed", ex);
        } catch (DataAccessException ex) {
            throw new AiServiceException("Reading could not be persisted due to invalid AI response", ex);
        }
    }

    private Map<String, Object> buildPayload(List<String> sourceWords, String instruction) {
        String wordsText = String.join(", ", sourceWords);
        String userPrompt = """
                Generate a B1-B2 level English reading passage using ALL of these vocabulary words: %s

                Return ONLY this JSON structure, nothing else:
                {
                  "title": "Short, clear title for the passage (max 8 words)",
                  "passage_en": "1-2 paragraph passage that uses every vocabulary word at least once",
                  "passage_tr": "Turkish translation of the passage",
                  "target_words": [
                    {"word": "<target_word_from_list>", "meaning_tr": "<turkish_meaning>"}
                  ],
                  "extra_words": [
                    {"word": "<difficult_non_target_word_from_passage>", "meaning_tr": "<turkish_meaning>"}
                  ],
                  "questions": [
                    {
                      "question": "Comprehension question about the passage",
                      "options": [
                        {"id": "A", "text": "Option text"},
                        {"id": "B", "text": "Option text"},
                        {"id": "C", "text": "Option text"},
                        {"id": "D", "text": "Option text"}
                      ],
                      "answer": "A"
                    }
                  ]
                }

                Rules:
                - Use ALL %d vocabulary words in the passage
                - title must summarize the passage topic clearly and be max 8 words
                - target_words must include all given vocabulary words
                - extra_words must be 0 to 5 items
                - extra_words must be selected from difficult words that appear in the passage
                - extra_words must NOT duplicate any target_words item
                - Do not always output the same extra word; choose based on the generated passage
                - EXACTLY 5 questions
                - Each question has EXACTLY 4 options (A, B, C, D)
                - Only ONE correct answer per question
                - Output ONLY the JSON object, nothing else
                """.formatted(wordsText, sourceWords.size());

        if (instruction != null && !instruction.isBlank()) {
            userPrompt = userPrompt + "\nAdditional instructions:\n- " + instruction.trim();
        }

        return Map.of(
                "model", model,
                "stream", false,
                "format", "json",
                "think", think,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
    }

    private ReadingContentResponse parseReading(String rawResponse) {
        JsonNode responseNode;
        try {
            responseNode = OBJECT_MAPPER.readTree(rawResponse);
        } catch (JsonProcessingException ex) {
            throw new AiServiceException("AI API response is not valid JSON", ex);
        }

        JsonNode contentNode = responseNode.path("message").path("content");
        if (contentNode.isMissingNode() || contentNode.isNull()) {
            throw new AiServiceException("AI response does not contain message.content");
        }

        try {
            if (!contentNode.isTextual()) {
                return OBJECT_MAPPER.treeToValue(contentNode, ReadingContentResponse.class);
            }

            String cleanedContent = cleanJsonFence(contentNode.asText());
            return OBJECT_MAPPER.readValue(cleanedContent, ReadingContentResponse.class);
        } catch (JsonProcessingException ex) {
            throw new AiServiceException("AI response content is not a valid reading JSON", ex);
        }
    }

    private String cleanJsonFence(String content) {
        String trimmed = content == null ? "" : content.trim();
        return trimmed
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
    }

    private void validateReading(ReadingContentResponse reading) {
        if (reading == null) {
            throw new AiServiceException("AI response is empty");
        }
        if (isBlank(reading.title())) {
            throw new AiServiceException("AI response must include a non-empty title");
        }
        if (isBlank(reading.passage_en()) || isBlank(reading.passage_tr())) {
            throw new AiServiceException("AI response must include non-empty passage_en and passage_tr");
        }
        if (reading.questions() == null || reading.questions().size() != 5) {
            throw new AiServiceException("AI response must include exactly 5 questions");
        }
        for (ReadingQuestion question : reading.questions()) {
            if (question == null || isBlank(question.question()) || isBlank(question.answer())) {
                throw new AiServiceException("Each question must include non-empty question and answer");
            }
            if (question.options() == null || question.options().size() != 4) {
                throw new AiServiceException("Each question must have exactly 4 options");
            }

            List<String> optionIds = question.options().stream()
                    .map(option -> option == null ? null : option.id())
                    .toList();

            for (ReadingQuestionOption option : question.options()) {
                if (option == null || isBlank(option.id()) || isBlank(option.text())) {
                    throw new AiServiceException("Each option must include non-empty id and text");
                }
            }

            long distinctOptionCount = optionIds.stream()
                    .filter(id -> id != null && !id.isBlank())
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .distinct()
                    .count();

            if (distinctOptionCount != 4) {
                throw new AiServiceException("Each question must have 4 distinct option ids");
            }

            String normalizedAnswer = question.answer().trim().toUpperCase();
            boolean answerExistsInOptions = optionIds.stream()
                    .filter(id -> id != null && !id.isBlank())
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .anyMatch(normalizedAnswer::equals);

            if (!answerExistsInOptions) {
                throw new AiServiceException("Question answer must match one of the option ids");
            }
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ReadingContentResponse normalizeReading(List<String> sourceWords, ReadingContentResponse parsedReading) {
        String normalizedTitle = normalizeTitle(sourceWords, parsedReading.title());

        Map<String, String> aiMeanings = (parsedReading.target_words() == null ? List.<ReadingWordItem>of() : parsedReading.target_words())
                .stream()
                .filter(item -> item.word() != null && !item.word().isBlank())
                .collect(Collectors.toMap(
                        item -> item.word().trim().toLowerCase(),
                        item -> item.meaning_tr() == null ? "" : item.meaning_tr().trim(),
                        (left, right) -> left
                ));

        Map<String, String> dbMeanings = wordRepository.findByEnglishIn(sourceWords).stream()
                .collect(Collectors.toMap(
                        word -> word.getEnglish().trim().toLowerCase(),
                        word -> word.getTurkish() == null ? "" : word.getTurkish().trim(),
                        (left, right) -> left
                ));

        List<ReadingWordItem> normalizedTargetWords = sourceWords.stream()
                .map(String::trim)
                .map(sourceWord -> {
                    String key = sourceWord.toLowerCase();
                    String meaning = aiMeanings.getOrDefault(key, dbMeanings.getOrDefault(key, ""));
                    return new ReadingWordItem(sourceWord, meaning);
                })
                .toList();

        List<ReadingWordItem> extraWords = parsedReading.extra_words() == null ? List.of() : parsedReading.extra_words();

        return new ReadingContentResponse(
                normalizedTitle,
                parsedReading.passage_en(),
                parsedReading.passage_tr(),
                normalizedTargetWords,
                extraWords,
                parsedReading.questions()
        );
    }

    private String normalizeTitle(List<String> sourceWords, String aiTitle) {
        if (aiTitle != null && !aiTitle.isBlank()) {
            String cleaned = aiTitle.trim().replaceAll("\\s+", " ");
            if (!cleaned.isBlank()) {
                return cleaned;
            }
        }

        List<String> words = sourceWords.stream()
                .filter(word -> word != null && !word.isBlank())
                .limit(4)
                .toList();

        if (words.isEmpty()) {
            return "Reading Passage";
        }

        return "Reading: " + String.join(", ", words);
    }

    @Transactional(readOnly = true)
    public Page<ReadingSummaryResponse> getReadings(Integer page, Integer size) {
        int pageNo = page == null ? 0 : Math.max(0, page);
        int pageSize = size == null ? 20 : Math.max(1, size);

        return readingRepository.findAll(
                        PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public ReadingDetailResponse getReadingById(Long id) {
        Reading reading = readingRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading not found: " + id));
        return toDetailResponse(reading);
    }

    private void saveReading(List<String> sourceWords, ReadingContentResponse response) {
        Reading reading = new Reading();
        reading.setTitle(response.title());
        reading.setPassageEn(response.passage_en());
        reading.setPassageTr(response.passage_tr());

        for (int i = 0; i < sourceWords.size(); i++) {
            ReadingSourceWord sourceWord = new ReadingSourceWord();
            sourceWord.setPositionIndex(i);
            sourceWord.setWord(sourceWords.get(i));
            reading.addSourceWord(sourceWord);
        }

        List<ReadingWordItem> targetWords = response.target_words() == null ? List.of() : response.target_words();
        for (int i = 0; i < targetWords.size(); i++) {
            ReadingWordItem item = targetWords.get(i);
            ReadingVocabularyWord word = new ReadingVocabularyWord();
            word.setWordType(ReadingWordType.TARGET);
            word.setPositionIndex(i);
            word.setWord(item.word());
            word.setMeaningTr(item.meaning_tr());
            reading.addVocabularyWord(word);
        }

        List<ReadingWordItem> extraWords = response.extra_words() == null ? List.of() : response.extra_words();
        for (int i = 0; i < extraWords.size(); i++) {
            ReadingWordItem item = extraWords.get(i);
            ReadingVocabularyWord word = new ReadingVocabularyWord();
            word.setWordType(ReadingWordType.EXTRA);
            word.setPositionIndex(i);
            word.setWord(item.word());
            word.setMeaningTr(item.meaning_tr());
            reading.addVocabularyWord(word);
        }

        for (int i = 0; i < response.questions().size(); i++) {
            ReadingQuestion questionItem = response.questions().get(i);
            ReadingQuestionEntity question = new ReadingQuestionEntity();
            question.setPositionIndex(i);
            question.setQuestionText(questionItem.question());
            question.setAnswerId(questionItem.answer());

            for (ReadingQuestionOption optionItem : questionItem.options()) {
                ReadingQuestionOptionEntity option = new ReadingQuestionOptionEntity();
                option.setOptionId(optionItem.id());
                option.setOptionText(optionItem.text());
                question.addOption(option);
            }

            reading.addQuestion(question);
        }

        readingRepository.save(reading);
    }

    private ReadingSummaryResponse toSummaryResponse(Reading reading) {
        List<String> sourceWords = reading.getSourceWords().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ReadingSourceWord::getPositionIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(ReadingSourceWord::getWord)
                .filter(Objects::nonNull)
                .toList();

        return new ReadingSummaryResponse(
                reading.getId(),
                reading.getCreatedAt(),
                resolveStoredTitle(reading.getTitle(), sourceWords),
                sourceWords
        );
    }

    private ReadingDetailResponse toDetailResponse(Reading reading) {
        List<String> sourceWords = reading.getSourceWords().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ReadingSourceWord::getPositionIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(ReadingSourceWord::getWord)
                .filter(Objects::nonNull)
                .toList();

        List<ReadingWordItem> targetWords = reading.getVocabularyWords().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getWordType() == ReadingWordType.TARGET)
                .sorted(Comparator.comparing(ReadingVocabularyWord::getPositionIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(item -> new ReadingWordItem(
                        item.getWord() == null ? "" : item.getWord(),
                        item.getMeaningTr() == null ? "" : item.getMeaningTr()
                ))
                .toList();

        List<ReadingWordItem> extraWords = reading.getVocabularyWords().stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getWordType() == ReadingWordType.EXTRA)
                .sorted(Comparator.comparing(ReadingVocabularyWord::getPositionIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(item -> new ReadingWordItem(
                        item.getWord() == null ? "" : item.getWord(),
                        item.getMeaningTr() == null ? "" : item.getMeaningTr()
                ))
                .toList();

        List<ReadingQuestion> questions = reading.getQuestions().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ReadingQuestionEntity::getPositionIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(question -> new ReadingQuestion(
                        question.getQuestionText() == null ? "" : question.getQuestionText(),
                        question.getOptions().stream()
                                .filter(Objects::nonNull)
                                .sorted(Comparator.comparing(ReadingQuestionOptionEntity::getOptionId, Comparator.nullsLast(String::compareTo)))
                                .map(option -> new ReadingQuestionOption(
                                        option.getOptionId() == null ? "" : option.getOptionId(),
                                        option.getOptionText() == null ? "" : option.getOptionText()
                                ))
                                .toList(),
                        question.getAnswerId() == null ? "" : question.getAnswerId()
                ))
                .toList();

        ReadingContentResponse content = new ReadingContentResponse(
                resolveStoredTitle(reading.getTitle(), sourceWords),
                reading.getPassageEn(),
                reading.getPassageTr(),
                targetWords,
                extraWords,
                questions
        );

        return new ReadingDetailResponse(
                reading.getId(),
                reading.getCreatedAt(),
                sourceWords,
                content
        );
    }

    private String resolveStoredTitle(String title, List<String> sourceWords) {
        return normalizeTitle(sourceWords, title);
    }
}
