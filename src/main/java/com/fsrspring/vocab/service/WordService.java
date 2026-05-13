package com.fsrspring.vocab.service;

import com.fsrspring.vocab.model.Word;
import com.fsrspring.vocab.repository.WordRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class WordService {

    private final WordRepository wordRepository;
    private final WordEnrichmentService wordEnrichmentService;

    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    public Page<Word> getWordsPage(
            String category,
            Word.DifficultyLevel difficulty,
            String search,
            Long topicId,
            com.fsrspring.vocab.model.CefrLevel cefrLevel,
            String partOfSpeech,
            int page,
            int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("id").ascending());
        return wordRepository.findAll(wordSpecification(category, difficulty, search, topicId, cefrLevel, partOfSpeech), pageRequest);
    }

    private Specification<Word> wordSpecification(
            String category,
            Word.DifficultyLevel difficulty,
            String search,
            Long topicId,
            com.fsrspring.vocab.model.CefrLevel cefrLevel,
            String partOfSpeech) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (difficulty != null) {
                predicates.add(cb.equal(root.get("difficulty"), difficulty));
            }
            if (topicId != null) {
                predicates.add(cb.equal(root.get("topic").get("id"), topicId));
            }
            if (cefrLevel != null) {
                predicates.add(cb.equal(root.get("cefrLevel"), cefrLevel));
            }
            if (partOfSpeech != null && !partOfSpeech.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.<String>get("partOfSpeech")), partOfSpeech.toLowerCase()));
            }
            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("word")), keyword),
                        cb.like(cb.lower(root.<String>get("translation")), keyword)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public Word getWordById(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Word not found with id: " + id));
    }

    public Word getWordByText(String word) {
        return wordRepository.findByWordIgnoreCase(word)
                .orElseThrow(() -> new NoSuchElementException("Word not found: " + word));
    }

    public Word getOrCreateWord(Word word) {
        return wordRepository.findByWordIgnoreCase(word.getWord())
                .orElseGet(() -> createWord(word));
    }

    public List<Word> getWordsByCategory(String category) {
        return wordRepository.findByCategory(category);
    }

    public List<Word> getWordsByDifficulty(Word.DifficultyLevel difficulty) {
        return wordRepository.findByDifficulty(difficulty);
    }

    public List<Word> getWordsByCategoryAndDifficulty(String category, Word.DifficultyLevel difficulty) {
        return wordRepository.findByCategoryAndDifficulty(category, difficulty);
    }

    public List<String> getAllCategories() {
        return wordRepository.findAllCategories();
    }

    public List<Word> getRandomWords(int limit) {
        return wordRepository.findRandomWords(limit);
    }

    public List<Word> searchWords(String keyword) {
        return wordRepository.searchByKeyword(keyword);
    }

    public Word createWord(Word word) {
        if (wordRepository.existsByWordIgnoreCase(word.getWord())) {
            throw new IllegalArgumentException("Word already exists: " + word.getWord());
        }
        Word saved = wordRepository.save(word);
        wordEnrichmentService.enqueueWord(saved.getId());
        return saved;
    }

    public List<Word> getWordsByTopic(Long topicId) {
        return wordRepository.findByTopicId(topicId);
    }

    public List<Word> getWordsByCefrLevel(com.fsrspring.vocab.model.CefrLevel cefrLevel) {
        return wordRepository.findByCefrLevel(cefrLevel);
    }

    public List<Word> getWordsByPartOfSpeech(String partOfSpeech) {
        return wordRepository.findByPartOfSpeechIgnoreCase(partOfSpeech);
    }

    public List<Word> getWordsByTopicAndCefr(Long topicId, com.fsrspring.vocab.model.CefrLevel cefrLevel) {
        return wordRepository.findByTopicIdAndCefrLevel(topicId, cefrLevel);
    }

    public List<String> getAllPartsOfSpeech() {
        return wordRepository.findAllPartsOfSpeech();
    }

    public Word updateWord(Long id, Word updatedWord) {
        Word existing = getWordById(id);
        existing.setWord(updatedWord.getWord());
        existing.setTranslation(updatedWord.getTranslation());
        existing.setExample(updatedWord.getExample());
        existing.setPronunciation(updatedWord.getPronunciation());
        existing.setCategory(updatedWord.getCategory());
        existing.setDifficulty(updatedWord.getDifficulty());
        existing.setImageUrl(updatedWord.getImageUrl());
        existing.setTopic(updatedWord.getTopic());
        existing.setCefrLevel(updatedWord.getCefrLevel());
        existing.setPartOfSpeech(updatedWord.getPartOfSpeech());
        existing.setAudioUrl(updatedWord.getAudioUrl());
        existing.setSynonyms(updatedWord.getSynonyms());
        existing.setAntonyms(updatedWord.getAntonyms());
        existing.setOrigin(updatedWord.getOrigin());
        return wordRepository.save(existing);
    }

    public void deleteWord(Long id) {
        if (!wordRepository.existsById(id)) {
            throw new NoSuchElementException("Word not found with id: " + id);
        }
        wordRepository.deleteById(id);
    }

    public long countWords() {
        return wordRepository.count();
    }
}
