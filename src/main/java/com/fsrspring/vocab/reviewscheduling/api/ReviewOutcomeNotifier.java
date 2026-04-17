package com.fsrspring.vocab.reviewscheduling.api;

public interface ReviewOutcomeNotifier {

    void onReviewApplied(ReviewOutcomeContext context);
}
