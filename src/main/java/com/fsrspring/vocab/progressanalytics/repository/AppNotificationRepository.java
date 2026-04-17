package com.fsrspring.vocab.progressanalytics.repository;

import com.fsrspring.vocab.progressanalytics.model.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {

    List<AppNotification> findTop50ByOrderByCreatedAtDesc();

    Optional<AppNotification> findTopByTypeOrderByCreatedAtDesc(AppNotification.NotificationType type);

    long countByIsReadFalse();
}
