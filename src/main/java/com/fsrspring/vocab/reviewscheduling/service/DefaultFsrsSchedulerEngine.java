package com.fsrspring.vocab.reviewscheduling.service;

import com.fsrspring.vocab.reviewscheduling.api.SchedulerEngine;
import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultFsrsSchedulerEngine implements SchedulerEngine {

    // FSRS-v6 defaults (from open-spaced-repetition references).
    private static final double[] P = {
            0.212, 1.2931, 2.3065, 8.2956, 6.4133, 0.8334, 3.0194,
            0.001, 1.8722, 0.1666, 0.796, 1.4835, 0.0614, 0.2629,
            1.6483, 0.6014, 1.8729, 0.5425, 0.0912, 0.0658, 0.1542
    };
    private static final double MIN_STABILITY = 0.1;
    private static final int MAX_INTERVAL_DAYS = 36500;

    @Value("${app.fsrs.request-retention:0.9}")
    private double requestRetention;

    private double clampDifficulty(double value) {
        return Math.max(1.0, Math.min(10.0, value));
    }

    private double clampStability(double value) {
        return Math.max(MIN_STABILITY, value);
    }

    private double initialDifficulty(ReviewRating rating) {
        double raw = P[4] - Math.exp(P[5] * (rating.value() - 1)) + 1;
        return clampDifficulty(raw);
    }

    private double linearDamping(double delta, double oldDifficulty) {
        return delta * (10.0 - oldDifficulty) / 9.0;
    }

    private double nextDifficulty(double currentDifficulty, ReviewRating rating) {
        double delta = -(P[6] * (rating.value() - 3));
        double next = currentDifficulty + linearDamping(delta, currentDifficulty);
        double reverted = P[7] * initialDifficulty(ReviewRating.EASY) + (1 - P[7]) * next;
        return clampDifficulty(reverted);
    }

    private double preReviewRetrievability(double elapsedDays, double stability) {
        double decay = -P[20];
        double factor = Math.pow(0.9, 1.0 / decay) - 1.0;
        return Math.pow(1 + factor * elapsedDays / clampStability(stability), decay);
    }

    private double nextForgetStability(double difficulty, double stability, double retrievability) {
        double sMin = stability / Math.exp(P[17] * P[18]);
        double raw = P[11]
                * Math.pow(difficulty, -P[12])
                * (Math.pow(stability + 1.0, P[13]) - 1.0)
                * Math.exp((1.0 - retrievability) * P[14]);
        return clampStability(Math.min(raw, sMin));
    }

    private double nextRecallStability(double difficulty, double stability, double retrievability, ReviewRating rating) {
        double hardPenalty = rating == ReviewRating.HARD ? P[15] : 1.0;
        double easyBonus = rating == ReviewRating.EASY ? P[16] : 1.0;
        double growth = Math.exp(P[8])
                * (11.0 - difficulty)
                * Math.pow(stability, -P[9])
                * (Math.exp((1.0 - retrievability) * P[10]) - 1.0)
                * hardPenalty
                * easyBonus;
        return clampStability(stability * (1.0 + growth));
    }

    @Override
    public void updateSequentialState(UserProgress progress, ReviewRating rating, long responseTimeMs) {
        final double alpha = 0.2;
        int ratingValue = rating.value();

        progress.setSequenceStep(progress.getSequenceStep() + 1);

        double accuracyInput = ratingValue >= 2 ? 1.0 : 0.0;
        double prevAcc = progress.getSequenceAccuracyEMA();
        progress.setSequenceAccuracyEMA(prevAcc == 0.0 ? accuracyInput : prevAcc * (1 - alpha) + accuracyInput * alpha);

        double responseInput = Math.max(0L, responseTimeMs);
        double prevResp = progress.getSequenceResponseMsEMA();
        progress.setSequenceResponseMsEMA(prevResp == 0.0 ? responseInput : prevResp * (1 - alpha) + responseInput * alpha);

        double consistencyInput = ratingValue >= 2 ? 1.0 : -1.0;
        double prevConsistency = progress.getSequenceConsistency();
        progress.setSequenceConsistency(prevConsistency * (1 - alpha) + consistencyInput * alpha);

        double difficultyTrendInput = 5.0 - ratingValue;
        double prevTrend = progress.getSequenceDifficultyTrend();
        progress.setSequenceDifficultyTrend(prevTrend * (1 - alpha) + difficultyTrendInput * alpha);
    }

    @Override
    public void updateFsrsState(UserProgress progress, ReviewRating rating, double lastIntervalHours) {
        double elapsedDays = Math.max(0.0, lastIntervalHours / 24.0);
        double previousDifficulty = clampDifficulty(progress.getFsrsDifficulty());
        double previousStability = clampStability(progress.getFsrsStability());
        double retrievability = Math.max(0.0, Math.min(1.0, preReviewRetrievability(elapsedDays, previousStability)));

        double nextDifficulty = nextDifficulty(previousDifficulty, rating);
        double nextStability = rating == ReviewRating.AGAIN
                ? nextForgetStability(previousDifficulty, previousStability, retrievability)
                : nextRecallStability(previousDifficulty, previousStability, retrievability, rating);

        progress.setFsrsDifficulty(nextDifficulty);
        progress.setFsrsStability(nextStability);
        progress.setFsrsRetrievability(retrievability);
    }

    @Override
    public LocalDateTime calculateNextReview(LocalDateTime now, UserProgress progress, ReviewRating rating) {
        if (rating == ReviewRating.AGAIN) {
            return now.plusMinutes(10);
        }

        double decay = -P[20];
        double factor = Math.pow(0.9, 1.0 / decay) - 1.0;
        double boundedRetention = Math.max(0.7, Math.min(0.99, requestRetention));
        double rawInterval = progress.getFsrsStability() / factor * (Math.pow(boundedRetention, 1.0 / decay) - 1.0);
        int intervalDays = (int) Math.max(1, Math.min(MAX_INTERVAL_DAYS, Math.round(rawInterval)));
        return now.plusDays(intervalDays);
    }
}
