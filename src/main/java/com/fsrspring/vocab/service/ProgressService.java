package com.fsrspring.vocab.service;

import com.fsrspring.vocab.model.UserProgress;
import com.fsrspring.vocab.model.Word;
import com.fsrspring.vocab.repository.UserProgressRepository;
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

    public UserProgress getOrCreateProgress(Word word) {
        return progressRepository.findByWord(word)
                .orElseGet(() -> {
                    UserProgress newProgress = UserProgress.builder()
                            .word(word)
                            .build();
                    return progressRepository.save(newProgress);
                });
    }

    public UserProgress recordAnswer(Word word, boolean correct) {
        UserProgress progress = getOrCreateProgress(word);
        if (correct) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
        } else {
            progress.setIncorrectCount(progress.getIncorrectCount() + 1);
        }
        progress.setLastStudied(LocalDateTime.now());
        progress.setNextReview(calculateNextReview(progress, correct));
        progress.setMastery(calculateMastery(progress));
        return progressRepository.save(progress);
    }

    private LocalDateTime calculateNextReview(UserProgress progress, boolean correct) {
        LocalDateTime now = LocalDateTime.now();
        int correctCount = progress.getCorrectCount();
        if (!correct) {
            return now.plusHours(1);
        }
        // Simple spaced repetition intervals
        return switch (correctCount) {
            case 0, 1 -> now.plusHours(4);
            case 2, 3 -> now.plusDays(1);
            case 4, 5 -> now.plusDays(3);
            case 6, 7 -> now.plusDays(7);
            default -> now.plusDays(14);
        };
    }

    private UserProgress.MasteryLevel calculateMastery(UserProgress progress) {
        int correct = progress.getCorrectCount();
        double accuracy = progress.getAccuracy();
        if (correct == 0) return UserProgress.MasteryLevel.NEW;
        if (correct < 3 || accuracy < 60) return UserProgress.MasteryLevel.LEARNING;
        if (correct < 8 || accuracy < 80) return UserProgress.MasteryLevel.REVIEWING;
        return UserProgress.MasteryLevel.MASTERED;
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
