package com.fsrspring.vocab.wordmanagement.repository;

import com.fsrspring.vocab.wordmanagement.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByCategory(String category);

    List<Word> findByDifficulty(Word.DifficultyLevel difficulty);

    List<Word> findByCategoryAndDifficulty(String category, Word.DifficultyLevel difficulty);

    @Query("SELECT DISTINCT w.category FROM Word w WHERE w.category IS NOT NULL AND w.category <> ''")
    List<String> findAllCategories();

    @Query("SELECT w FROM Word w ORDER BY RANDOM() LIMIT :limit")
    List<Word> findRandomWords(int limit);

    @Query("SELECT w FROM Word w WHERE LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(w.translation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Word> searchByKeyword(String keyword);
}
