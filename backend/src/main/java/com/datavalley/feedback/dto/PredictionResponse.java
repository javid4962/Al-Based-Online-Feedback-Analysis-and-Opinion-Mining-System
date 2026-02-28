package com.datavalley.feedback.dto;

import com.datavalley.feedback.enums.SentimentLabel;

import java.time.Instant;

public record PredictionResponse(
    Long feedbackId,
    SentimentLabel label,
    double confidence,
    String modelVersion,
    Instant inferredAt
) {
}
