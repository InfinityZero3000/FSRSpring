package com.fsrspring.vocab.reviewscheduling.model;

import com.fsrspring.vocab.wordmanagement.model.Word;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_events", indexes = {
        @Index(name = "idx_review_events_word_reviewed_at", columnList = "word_id,reviewedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false, updatable = false)
    private Word word;

    @Column(nullable = false, updatable = false)
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private ReviewRating ratingLabel;

    @Column(nullable = false, updatable = false)
    private Boolean correct;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Long responseTimeMs = 0L;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime reviewedAt = LocalDateTime.now();

    @Column(nullable = false, length = 40, updatable = false)
    @Builder.Default
    private String schedulerVersion = "fsrs-v1";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private FsrsCardState previousState = FsrsCardState.LEARNING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private FsrsCardState newState = FsrsCardState.LEARNING;

    @Column(updatable = false)
    private Integer previousStep;

    @Column(updatable = false)
    private Integer newStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private UserProgress.MasteryLevel previousMastery = UserProgress.MasteryLevel.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private UserProgress.MasteryLevel newMastery = UserProgress.MasteryLevel.NEW;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Double previousStability = 0.0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Double newStability = 0.0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Double previousDifficulty = 0.0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Double newDifficulty = 0.0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Double retrievabilityBefore = 0.0;

    @Column(updatable = false)
    private LocalDateTime previousNextReview;

    @Column(updatable = false)
    private LocalDateTime newNextReview;
}
