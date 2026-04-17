package com.fsrspring.vocab.progressanalytics.service;

import com.fsrspring.vocab.progressanalytics.model.AppNotification;
import com.fsrspring.vocab.progressanalytics.repository.AppNotificationRepository;
import com.fsrspring.vocab.reviewscheduling.api.ReviewOutcomeContext;
import com.fsrspring.vocab.reviewscheduling.api.ReviewOutcomeNotifier;
import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewOutcomeNotificationService implements ReviewOutcomeNotifier {

    private final AppNotificationRepository notificationRepository;

    @Override
    public void onReviewApplied(ReviewOutcomeContext context) {
        if (context.rating() == ReviewRating.AGAIN) {
            notificationRepository.save(AppNotification.builder()
                    .title("Review lapsed")
                    .message("You forgot '" + context.word() + "'. It was moved to relearning.")
                    .deepLink("/learn?mode=fsrs&wordId=" + context.wordId())
                    .type(AppNotification.NotificationType.FSRS_LAPSE)
                    .build());
        }

        if (context.previousMastery() != UserProgress.MasteryLevel.MASTERED
                && context.currentMastery() == UserProgress.MasteryLevel.MASTERED) {
            notificationRepository.save(AppNotification.builder()
                    .title("Word mastered")
                    .message("Great job! '" + context.word() + "' reached MASTERED level.")
                    .deepLink("/progress")
                    .type(AppNotification.NotificationType.FSRS_MASTERY)
                    .build());
        }
    }
}
