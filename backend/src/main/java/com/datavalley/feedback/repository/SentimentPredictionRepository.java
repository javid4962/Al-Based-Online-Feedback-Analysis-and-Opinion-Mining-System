package com.datavalley.feedback.repository;

import com.datavalley.feedback.entity.SentimentPrediction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SentimentPredictionRepository extends JpaRepository<SentimentPrediction, Long> {
    @EntityGraph(attributePaths = "modelVersion")
    Optional<SentimentPrediction> findTopByFeedbackItemIdOrderByInferredAtDesc(Long feedbackId);

    List<SentimentPrediction> findByFeedbackItemIdIn(List<Long> feedbackIds);

    @Query("select sp from SentimentPrediction sp join fetch sp.feedbackItem fi where fi.submittedAt between :from and :to")
    List<SentimentPrediction> findForSubmittedRange(@Param("from") Instant from, @Param("to") Instant to);
}
