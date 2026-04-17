package com.fsrspring.vocab.reviewscheduling.api;

import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;

import java.time.LocalDateTime;

public interface SchedulerEngine {

    void updateSequentialState(UserProgress progress, ReviewRating rating, long responseTimeMs);

    void updateFsrsState(UserProgress progress, ReviewRating rating, double lastIntervalHours);

    LocalDateTime calculateNextReview(LocalDateTime now, UserProgress progress, ReviewRating rating);
}
