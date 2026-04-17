package com.fsrspring.vocab.reviewscheduling.service;

import com.fsrspring.vocab.reviewscheduling.api.ReviewOutcomeContext;
import com.fsrspring.vocab.reviewscheduling.api.ReviewOutcomeNotifier;
import com.fsrspring.vocab.reviewscheduling.api.ReviewSchedulingService;
import com.fsrspring.vocab.reviewscheduling.api.SchedulerEngine;
import com.fsrspring.vocab.reviewscheduling.model.FsrsCardState;
import com.fsrspring.vocab.reviewscheduling.model.ReviewEvent;
import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;
import com.fsrspring.vocab.wordmanagement.model.Word;
import com.fsrspring.vocab.reviewscheduling.repository.ReviewEventRepository;
import com.fsrspring.vocab.reviewscheduling.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class FsrsService implements ReviewSchedulingService {

    private final UserProgressRepository progressRepository;
    private final ReviewEventRepository reviewEventRepository;
    private final SchedulerEngine schedulerEngine;
    private final ObjectProvider<ReviewOutcomeNotifier> reviewOutcomeNotifierProvider;

    private static final int LEARNING_STEP_MINUTES = 1;
    private static final int LEARNING_GOOD_STEP_MINUTES = 10;
    private static final int LEARNING_HARD_STEP_MINUTES = 5;
    private static final int RELEARNING_AGAIN_STEP_MINUTES = 10;

    @Override
    public UserProgress getOrCreateProgress(Word word) {
        return progressRepository.findByWord(word)
                .orElseGet(() -> progressRepository.save(UserProgress.builder().word(word).build()));
    }

    @Override
    public UserProgress reviewWord(Word word, ReviewRating rating, long responseTimeMs) {
        UserProgress progress = getOrCreateProgress(word);
        LocalDateTime now = LocalDateTime.now();
        int ratingValue = rating.value();
        UserProgress.MasteryLevel previousMastery = progress.getMastery();
        FsrsCardState previousState = progress.getFsrsCardState();
        Integer previousStep = progress.getFsrsStep();
        double previousStability = progress.getFsrsStability();
        double previousDifficulty = progress.getFsrsDifficulty();
        LocalDateTime previousNextReview = progress.getNextReview();
        if (previousState == null) {
            previousState = FsrsCardState.LEARNING;
            progress.setFsrsCardState(previousState);
        }
        if (previousStep == null) {
            previousStep = 0;
            progress.setFsrsStep(previousStep);
        }

        boolean correct = ratingValue >= 2;
        if (correct) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
            progress.setFsrsRepetition(progress.getFsrsRepetition() + 1);
        } else {
            progress.setIncorrectCount(progress.getIncorrectCount() + 1);
            progress.setFsrsLapseCount(progress.getFsrsLapseCount() + 1);
            progress.setFsrsRepetition(0);
        }

        Duration gap = progress.getLastStudied() == null
                ? Duration.ofHours(0)
                : Duration.between(progress.getLastStudied(), now);

        double lastIntervalHours = Math.max(0.0, gap.toMinutes() / 60.0);
        progress.setLastIntervalHours(lastIntervalHours);
        progress.setLastStudied(now);

        schedulerEngine.updateSequentialState(progress, rating, responseTimeMs);
        schedulerEngine.updateFsrsState(progress, rating, lastIntervalHours);

        progress.setMastery(calculateMastery(progress));
        applySchedulingFlow(progress, rating, now);

        UserProgress saved = progressRepository.save(progress);

        reviewEventRepository.save(ReviewEvent.builder()
            .word(word)
            .rating(ratingValue)
            .ratingLabel(rating)
            .correct(correct)
            .responseTimeMs(Math.max(0L, responseTimeMs))
            .reviewedAt(now)
            .schedulerVersion("fsrs-v1")
            .previousState(previousState)
            .newState(saved.getFsrsCardState())
            .previousStep(previousStep)
            .newStep(saved.getFsrsStep())
            .previousMastery(previousMastery)
            .newMastery(saved.getMastery())
            .previousStability(previousStability)
            .newStability(saved.getFsrsStability())
            .previousDifficulty(previousDifficulty)
            .newDifficulty(saved.getFsrsDifficulty())
            .retrievabilityBefore(saved.getFsrsRetrievability())
            .previousNextReview(previousNextReview)
            .newNextReview(saved.getNextReview())
            .build());

        long dueNowCount = progressRepository.countDueWords(LocalDateTime.now());
        reviewOutcomeNotifierProvider.ifAvailable(notifier -> notifier.onReviewApplied(new ReviewOutcomeContext(
            word.getId(),
            word.getWord(),
            rating,
            previousMastery,
            saved.getMastery(),
            saved.getNextReview(),
            dueNowCount
        )));

        return saved;
    }

    @Override
    public List<UserProgress> getDueWords(int limit) {
        List<UserProgress> due = progressRepository.findDueWords(LocalDateTime.now());
        if (limit > 0 && due.size() > limit) {
            return due.subList(0, limit);
        }
        return due;
    }

    @Override
    public Map<String, Object> getFsrsStats() {
        LocalDateTime now = LocalDateTime.now();
        long dueNow = progressRepository.countDueWords(now);
        long mastered = progressRepository.countMastered();
        long learning = progressRepository.countLearning();

        double retentionEstimate = 0.0;
        List<UserProgress> all = progressRepository.findAll();
        if (!all.isEmpty()) {
            retentionEstimate = all.stream()
                    .mapToDouble(UserProgress::getFsrsRetrievability)
                    .average()
                    .orElse(0.0) * 100.0;
        }

        return Map.of(
                "dueNow", dueNow,
                "mastered", mastered,
                "learning", learning,
                "retentionEstimate", Math.round(retentionEstimate * 10.0) / 10.0
        );
    }

    private UserProgress.MasteryLevel calculateMastery(UserProgress progress) {
        int correct = progress.getCorrectCount();
        double accuracy = progress.getAccuracy();

        if (correct == 0) {
            return UserProgress.MasteryLevel.NEW;
        }
        if (correct < 3 || accuracy < 60 || progress.getFsrsRetrievability() < 0.4) {
            return UserProgress.MasteryLevel.LEARNING;
        }
        if (correct < 8 || accuracy < 80 || progress.getFsrsRetrievability() < 0.75) {
            return UserProgress.MasteryLevel.REVIEWING;
        }
        return UserProgress.MasteryLevel.MASTERED;
    }

    private void applySchedulingFlow(UserProgress progress, ReviewRating rating, LocalDateTime now) {
        FsrsCardState state = progress.getFsrsCardState() == null
                ? FsrsCardState.LEARNING
                : progress.getFsrsCardState();

        int step = progress.getFsrsStep() == null ? 0 : progress.getFsrsStep();

        if (state == FsrsCardState.LEARNING) {
            applyLearningTransition(progress, rating, now, step, false);
            return;
        }

        if (state == FsrsCardState.RELEARNING) {
            applyLearningTransition(progress, rating, now, step, true);
            return;
        }

        if (rating == ReviewRating.AGAIN) {
            progress.setFsrsCardState(FsrsCardState.RELEARNING);
            progress.setFsrsStep(0);
            progress.setNextReview(now.plusMinutes(RELEARNING_AGAIN_STEP_MINUTES));
            return;
        }

        progress.setFsrsCardState(FsrsCardState.REVIEW);
        progress.setFsrsStep(null);
        progress.setNextReview(schedulerEngine.calculateNextReview(now, progress, rating));
    }

    private void applyLearningTransition(
            UserProgress progress,
            ReviewRating rating,
            LocalDateTime now,
            int step,
            boolean relearning
    ) {
        if (rating == ReviewRating.AGAIN) {
            progress.setFsrsCardState(relearning ? FsrsCardState.RELEARNING : FsrsCardState.LEARNING);
            progress.setFsrsStep(0);
            progress.setNextReview(now.plusMinutes(relearning ? RELEARNING_AGAIN_STEP_MINUTES : LEARNING_STEP_MINUTES));
            return;
        }

        if (rating == ReviewRating.HARD) {
            progress.setFsrsCardState(relearning ? FsrsCardState.RELEARNING : FsrsCardState.LEARNING);
            progress.setFsrsStep(step);
            progress.setNextReview(now.plusMinutes(LEARNING_HARD_STEP_MINUTES));
            return;
        }

        if (rating == ReviewRating.GOOD && step == 0) {
            progress.setFsrsCardState(relearning ? FsrsCardState.RELEARNING : FsrsCardState.LEARNING);
            progress.setFsrsStep(1);
            progress.setNextReview(now.plusMinutes(LEARNING_GOOD_STEP_MINUTES));
            return;
        }

        progress.setFsrsCardState(FsrsCardState.REVIEW);
        progress.setFsrsStep(null);
        progress.setNextReview(schedulerEngine.calculateNextReview(now, progress, rating));
    }
}
