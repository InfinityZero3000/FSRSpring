package com.fsrspring.vocab.reviewscheduling.api;

import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;

import java.time.LocalDateTime;

public record ReviewOutcomeContext(
        Long wordId,
        String word,
        ReviewRating rating,
        UserProgress.MasteryLevel previousMastery,
        UserProgress.MasteryLevel currentMastery,
        LocalDateTime nextReviewAt,
        long dueNowCount
) {
}
