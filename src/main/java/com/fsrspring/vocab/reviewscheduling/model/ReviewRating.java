package com.fsrspring.vocab.reviewscheduling.model;

import java.util.Locale;

public enum ReviewRating {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4);

    private final int value;

    ReviewRating(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static ReviewRating fromValue(int value) {
        return switch (value) {
            case 1 -> AGAIN;
            case 2 -> HARD;
            case 3 -> GOOD;
            case 4 -> EASY;
            default -> throw new IllegalArgumentException("Rating must be between 1 and 4");
        };
    }

    public static ReviewRating fromInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Rating is required");
        }

        try {
            return fromValue(Integer.parseInt(input));
        } catch (NumberFormatException ignored) {
            return ReviewRating.valueOf(input.trim().toUpperCase(Locale.ROOT));
        }
    }
}
