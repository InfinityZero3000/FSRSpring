package com.fsrspring.vocab.wordmanagement.service;

import com.fsrspring.vocab.wordmanagement.api.WordManagementService;
import com.fsrspring.vocab.wordmanagement.model.Word;
import com.fsrspring.vocab.wordmanagement.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class WordService implements WordManagementService {

    private final WordRepository wordRepository;

    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    public Word getWordById(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Word not found with id: " + id));
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
        return wordRepository.save(word);
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
