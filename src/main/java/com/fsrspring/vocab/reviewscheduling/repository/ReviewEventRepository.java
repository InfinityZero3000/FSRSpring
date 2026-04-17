package com.fsrspring.vocab.reviewscheduling.repository;

import com.fsrspring.vocab.reviewscheduling.model.ReviewEvent;
import com.fsrspring.vocab.wordmanagement.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewEventRepository extends JpaRepository<ReviewEvent, Long> {

    List<ReviewEvent> findTop20ByWordOrderByReviewedAtDesc(Word word);
}
