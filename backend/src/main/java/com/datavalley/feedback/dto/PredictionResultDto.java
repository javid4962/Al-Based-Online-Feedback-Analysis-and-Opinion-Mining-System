package com.datavalley.feedback.dto;

import com.datavalley.feedback.enums.SentimentLabel;

public record PredictionResultDto(
    SentimentLabel label,
    double confidence,
    String logitsJson
) {
}
