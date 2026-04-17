package com.fsrspring.vocab.learningsession.quiz.repository;

import com.fsrspring.vocab.learningsession.quiz.model.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {

    List<QuizSession> findTop10ByOrderByStartedAtDesc();

    @Query("SELECT AVG(qs.correctAnswers * 100.0 / qs.totalQuestions) FROM QuizSession qs WHERE qs.completedAt IS NOT NULL")
    Double findAverageScore();

    @Query("SELECT COUNT(qs) FROM QuizSession qs WHERE qs.completedAt IS NOT NULL")
    long countCompleted();
}
