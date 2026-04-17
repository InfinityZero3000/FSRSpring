package com.fsrspring.vocab.progressanalytics.service;

import com.fsrspring.vocab.reviewscheduling.api.ReviewSchedulingService;
import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;
import com.fsrspring.vocab.wordmanagement.model.Word;
import com.fsrspring.vocab.reviewscheduling.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final ReviewSchedulingService reviewSchedulingService;

    public UserProgress getOrCreateProgress(Word word) {
        return reviewSchedulingService.getOrCreateProgress(word);
    }

    public UserProgress recordAnswer(Word word, boolean correct) {
        ReviewRating rating = correct ? ReviewRating.GOOD : ReviewRating.AGAIN;
        return reviewSchedulingService.reviewWord(word, rating, 0L);
    }

    public List<UserProgress> getWordsForReview() {
        return progressRepository.findWordsForReview(LocalDateTime.now());
    }

    public List<UserProgress> getAllProgress() {
        return progressRepository.findAll();
    }

    public UserProgress getProgressByWordId(Long wordId) {
        return progressRepository.findByWordId(wordId).orElse(null);
    }

    public long countMastered() {
        return progressRepository.countMastered();
    }

    public long countLearning() {
        return progressRepository.countLearning();
    }

    public long totalCorrect() {
        Long val = progressRepository.sumCorrectAnswers();
        return val != null ? val : 0L;
    }

    public long totalIncorrect() {
        Long val = progressRepository.sumIncorrectAnswers();
        return val != null ? val : 0L;
    }
}
