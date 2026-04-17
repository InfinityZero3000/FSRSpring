package com.fsrspring.vocab.wordmanagement.controller;

import com.fsrspring.vocab.wordmanagement.api.WordManagementService;
import com.fsrspring.vocab.wordmanagement.model.Word;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/word-management/words")
@RequiredArgsConstructor
public class WordController {

    private final WordManagementService wordService;

    @GetMapping
    public ResponseEntity<List<Word>> getAllWords(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Word.DifficultyLevel difficulty,
            @RequestParam(required = false) String search) {
        List<Word> words;
        if (search != null && !search.isBlank()) {
            words = wordService.searchWords(search);
        } else if (category != null && difficulty != null) {
            words = wordService.getWordsByCategoryAndDifficulty(category, difficulty);
        } else if (category != null) {
            words = wordService.getWordsByCategory(category);
        } else if (difficulty != null) {
            words = wordService.getWordsByDifficulty(difficulty);
        } else {
            words = wordService.getAllWords();
        }
        return ResponseEntity.ok(words);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Word> getWordById(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getWordById(id));
    }

    @GetMapping("/random")
    public ResponseEntity<List<Word>> getRandomWords(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(wordService.getRandomWords(limit));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(wordService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<Word> createWord(@Valid @RequestBody Word word) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wordService.createWord(word));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Word> updateWord(@PathVariable Long id, @Valid @RequestBody Word word) {
        return ResponseEntity.ok(wordService.updateWord(id, word));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long id) {
        wordService.deleteWord(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWordCount() {
        return ResponseEntity.ok(Map.of("count", wordService.countWords()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }
}
