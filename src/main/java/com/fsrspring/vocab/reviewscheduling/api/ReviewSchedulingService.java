package com.fsrspring.vocab.reviewscheduling.api;

import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.reviewscheduling.model.UserProgress;
import com.fsrspring.vocab.wordmanagement.model.Word;

import java.util.List;
import java.util.Map;

public interface ReviewSchedulingService {

    UserProgress getOrCreateProgress(Word word);

    UserProgress reviewWord(Word word, ReviewRating rating, long responseTimeMs);

    List<UserProgress> getDueWords(int limit);

    Map<String, Object> getFsrsStats();
}
