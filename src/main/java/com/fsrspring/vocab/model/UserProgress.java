package com.fsrspring.vocab.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(nullable = false)
    @Builder.Default
    private Integer correctCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer incorrectCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MasteryLevel mastery = MasteryLevel.NEW;

    @Column
    private LocalDateTime lastStudied;

    @Column
    private LocalDateTime nextReview;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum MasteryLevel {
        NEW, LEARNING, REVIEWING, MASTERED
    }

    public int getTotalAttempts() {
        return correctCount + incorrectCount;
    }

    public double getAccuracy() {
        int total = getTotalAttempts();
        if (total == 0) return 0.0;
        return (double) correctCount / total * 100;
    }
}
