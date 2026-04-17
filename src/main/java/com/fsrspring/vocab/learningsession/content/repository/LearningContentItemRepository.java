package com.fsrspring.vocab.learningsession.content.repository;

import com.fsrspring.vocab.learningsession.content.model.LearningContentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningContentItemRepository extends JpaRepository<LearningContentItem, Long> {

    List<LearningContentItem> findTop30ByTopicOrderByPublishedAtDesc(String topic);
}
