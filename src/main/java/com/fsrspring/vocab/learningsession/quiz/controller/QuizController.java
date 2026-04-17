package com.fsrspring.vocab.learningsession.quiz.controller;

import com.fsrspring.vocab.learningsession.quiz.model.QuizSession;
import com.fsrspring.vocab.wordmanagement.api.WordManagementService;
import com.fsrspring.vocab.wordmanagement.model.Word;
import com.fsrspring.vocab.learningsession.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-session/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final WordManagementService wordService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startQuiz(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Word.DifficultyLevel difficulty) {
        List<Word> words;
        if (category != null && difficulty != null) {
            words = wordService.getWordsByCategoryAndDifficulty(category, difficulty);
        } else if (category != null) {
            words = wordService.getWordsByCategory(category);
        } else if (difficulty != null) {
            words = wordService.getWordsByDifficulty(difficulty);
        } else {
            words = wordService.getRandomWords(count);
        }
        if (words.size() > count) {
            words = words.subList(0, count);
        }
        QuizSession session = quizService.startSession(words.size(), category, difficulty);
        return ResponseEntity.ok(Map.of(
                "sessionId", session.getId(),
                "words", words,
                "totalQuestions", words.size()
        ));
    }

    @PostMapping("/session/{sessionId}/answer")
    public ResponseEntity<QuizSession> submitAnswer(
            @PathVariable Long sessionId,
            @RequestParam Long wordId,
            @RequestParam boolean correct) {
        QuizSession session = quizService.submitAnswer(sessionId, wordId, correct);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/session/{sessionId}/complete")
    public ResponseEntity<QuizSession> completeSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(quizService.completeSession(sessionId));
    }

    @GetMapping("/sessions/recent")
    public ResponseEntity<List<QuizSession>> getRecentSessions() {
        return ResponseEntity.ok(quizService.getRecentSessions());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQuizStats() {
        Double avgScore = quizService.getAverageScore();
        long completed = quizService.countCompletedSessions();
        return ResponseEntity.ok(Map.of(
                "completedSessions", completed,
                "averageScore", avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0
        ));
    }
}
