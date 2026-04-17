package com.fsrspring.vocab.reviewscheduling.controller;

import com.fsrspring.vocab.reviewscheduling.api.ReviewSchedulingService;
import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;
import com.fsrspring.vocab.wordmanagement.api.WordManagementService;
import com.fsrspring.vocab.wordmanagement.model.Word;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review-scheduling/fsrs")
@RequiredArgsConstructor
public class FsrsController {

    private final ReviewSchedulingService reviewSchedulingService;
    private final WordManagementService wordService;

    @GetMapping("/due")
    public ResponseEntity<List<UserProgress>> getDueWords(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(reviewSchedulingService.getDueWords(limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFsrsStats() {
        return ResponseEntity.ok(reviewSchedulingService.getFsrsStats());
    }

    @PostMapping("/review")
    public ResponseEntity<UserProgress> review(
            @RequestParam Long wordId,
            @RequestParam String rating,
            @RequestParam(defaultValue = "0") long responseMs) {
        Word word = wordService.getWordById(wordId);
        ReviewRating reviewRating = ReviewRating.fromInput(rating);
        return ResponseEntity.ok(reviewSchedulingService.reviewWord(word, reviewRating, responseMs));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }
}
