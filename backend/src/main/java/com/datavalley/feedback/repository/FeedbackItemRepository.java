package com.datavalley.feedback.repository;

import com.datavalley.feedback.entity.FeedbackItem;
import com.datavalley.feedback.enums.SourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface FeedbackItemRepository extends JpaRepository<FeedbackItem, Long> {
    Page<FeedbackItem> findBySourceType(SourceType sourceType, Pageable pageable);

    Page<FeedbackItem> findBySubmittedAtBetween(Instant from, Instant to, Pageable pageable);

    Page<FeedbackItem> findBySourceTypeAndSubmittedAtBetween(SourceType sourceType, Instant from, Instant to, Pageable pageable);

    List<FeedbackItem> findAllBySubmittedAtBetween(Instant from, Instant to);
}
