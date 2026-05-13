package com.fsrspring.vocab.service;

import com.fsrspring.vocab.model.TrustedFlashcard;
import com.fsrspring.vocab.model.Word;
import com.fsrspring.vocab.repository.TrustedFlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FlashcardService {

    private static final Map<String, String> LEGACY_TRANSLATION_FIXES = Map.of(
            "kien cuong", "kiên cường",
            "dien dat ro rang", "diễn đạt rõ ràng",
            "su thau hieu", "sự thấu hiểu"
    );

    private final TrustedFlashcardRepository flashcardRepository;
    private final WordService wordService;

    public List<TrustedFlashcard> list(String search) {
        normalizeLegacyTranslations();
        List<TrustedFlashcard> cards;
        if (search != null && !search.isBlank()) {
            cards = flashcardRepository.searchByKeyword(search);
        } else {
            cards = flashcardRepository.findTop100ByOrderByImportedAtDesc();
        }
        return uniqueByWord(cards);
    }

    public List<TrustedFlashcard> importTrustedSet(String sourceName, String topic) {
        List<TrustedFlashcard> seeded = new ArrayList<>();

        List<Map<String, String>> defaults = List.of(
            Map.of("word", "resilient", "translation", "kiên cường", "example", "She is resilient in difficult times."),
            Map.of("word", "articulate", "translation", "diễn đạt rõ ràng", "example", "He can articulate complex ideas clearly."),
            Map.of("word", "insight", "translation", "sự thấu hiểu", "example", "The report provides useful insight.")
        );

        for (Map<String, String> item : defaults) {
            TrustedFlashcard flashcard = flashcardRepository
                    .findBySourceNameIgnoreCaseAndTopicIgnoreCaseAndWordIgnoreCase(sourceName, topic, item.get("word"))
                    .orElseGet(() -> TrustedFlashcard.builder()
                            .word(item.get("word"))
                            .topic(topic)
                            .level("B1")
                            .sourceName(sourceName)
                            .sourceUrl("https://" + sourceName.toLowerCase().replace(" ", "") + ".com")
                            .build());

            flashcard.setTranslation(item.get("translation"));
            flashcard.setExample(item.get("example"));
            flashcard.setLevel("B1");
            seeded.add(flashcardRepository.save(flashcard));
        }

        return uniqueByWord(seeded);
    }

    public Word saveToVocabulary(Long flashcardId, Word.DifficultyLevel difficulty, String category) {
        TrustedFlashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found: " + flashcardId));

        Word word = Word.builder()
                .word(flashcard.getWord())
                .translation(flashcard.getTranslation())
                .example(flashcard.getExample())
                .difficulty(difficulty)
                .category(category)
                .build();

        return wordService.getOrCreateWord(word);
    }

    private void normalizeLegacyTranslations() {
        List<TrustedFlashcard> candidates = flashcardRepository.findTop100ByOrderByImportedAtDesc();
        for (TrustedFlashcard flashcard : candidates) {
            String raw = flashcard.getTranslation();
            if (raw == null || raw.isBlank()) {
                continue;
            }

            String normalized = LEGACY_TRANSLATION_FIXES.get(raw.trim().toLowerCase(Locale.ROOT));
            if (normalized != null && !normalized.equals(raw)) {
                flashcard.setTranslation(normalized);
                flashcardRepository.save(flashcard);
            }
        }
    }

    private List<TrustedFlashcard> uniqueByWord(List<TrustedFlashcard> cards) {
        Map<String, TrustedFlashcard> unique = new LinkedHashMap<>();
        for (TrustedFlashcard card : cards) {
            String key = card.getWord() == null ? "" : card.getWord().trim().toLowerCase(Locale.ROOT);
            if (!key.isBlank()) {
                unique.putIfAbsent(key, card);
            }
        }
        return new ArrayList<>(unique.values());
    }
}
