package com.fsrspring.vocab.learningsession.quiz.service;

import com.fsrspring.vocab.learningsession.quiz.model.QuizSession;
import com.fsrspring.vocab.reviewscheduling.api.ReviewSchedulingService;
import com.fsrspring.vocab.reviewscheduling.model.ReviewRating;
import com.fsrspring.vocab.wordmanagement.api.WordManagementService;
import com.fsrspring.vocab.wordmanagement.model.Word;
import com.fsrspring.vocab.learningsession.quiz.repository.QuizSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizSessionRepository quizSessionRepository;
    private final WordManagementService wordService;
    private final ReviewSchedulingService reviewSchedulingService;

    public QuizSession startSession(int questionCount, String category, Word.DifficultyLevel difficulty) {
        QuizSession session = QuizSession.builder()
                .totalQuestions(questionCount)
                .category(category)
                .difficulty(difficulty)
                .build();
        return quizSessionRepository.save(session);
    }

    public QuizSession submitAnswer(Long sessionId, Long wordId, boolean correct) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz session not found: " + sessionId));
        Word word = wordService.getWordById(wordId);
        reviewSchedulingService.reviewWord(word, correct ? ReviewRating.GOOD : ReviewRating.AGAIN, 0L);
        if (correct) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        } else {
            session.setIncorrectAnswers(session.getIncorrectAnswers() + 1);
        }
        return quizSessionRepository.save(session);
    }

    public QuizSession completeSession(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz session not found: " + sessionId));
        session.setCompletedAt(LocalDateTime.now());
        return quizSessionRepository.save(session);
    }

    public List<QuizSession> getRecentSessions() {
        return quizSessionRepository.findTop10ByOrderByStartedAtDesc();
    }

    public Double getAverageScore() {
        return quizSessionRepository.findAverageScore();
    }

    public long countCompletedSessions() {
        return quizSessionRepository.countCompleted();
    }
}
