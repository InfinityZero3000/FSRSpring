package com.fsrspring.vocab.wordmanagement.api;

import com.fsrspring.vocab.wordmanagement.model.Word;

import java.util.List;

public interface WordManagementService {

    List<Word> getAllWords();

    Word getWordById(Long id);

    List<Word> getWordsByCategory(String category);

    List<Word> getWordsByDifficulty(Word.DifficultyLevel difficulty);

    List<Word> getWordsByCategoryAndDifficulty(String category, Word.DifficultyLevel difficulty);

    List<String> getAllCategories();

    List<Word> getRandomWords(int limit);

    List<Word> searchWords(String keyword);

    Word createWord(Word word);

    Word updateWord(Long id, Word updatedWord);

    void deleteWord(Long id);

    long countWords();
}