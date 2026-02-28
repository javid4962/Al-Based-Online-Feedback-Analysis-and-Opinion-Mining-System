package com.datavalley.feedback.dto;

import com.datavalley.feedback.enums.SentimentLabel;
import com.datavalley.feedback.enums.SourceType;

import java.time.Instant;

public record FeedbackListItem(
    Long feedbackId,
    SourceType sourceType,
    String sourceRef,
    String text,
    Integer rating,
    String locale,
    Instant submittedAt,
    SentimentLabel sentiment,
    Double confidence
) {
}
