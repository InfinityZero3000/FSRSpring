package com.fsrspring.vocab.learningsession.content.controller;

import com.fsrspring.vocab.learningsession.content.model.LearningContentItem;
import com.fsrspring.vocab.learningsession.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-session/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/youtube")
    public ResponseEntity<List<LearningContentItem>> youtube(@RequestParam(defaultValue = "vocabulary") String topic) {
        return ResponseEntity.ok(contentService.fetchYoutube(topic));
    }

    @GetMapping("/news")
    public ResponseEntity<List<LearningContentItem>> news(@RequestParam(defaultValue = "vocabulary") String topic) {
        return ResponseEntity.ok(contentService.fetchNews(topic));
    }

    @GetMapping("/combined")
    public ResponseEntity<Map<String, Object>> combined(@RequestParam(defaultValue = "vocabulary") String topic) {
        return ResponseEntity.ok(contentService.fetchCombined(topic));
    }
}
