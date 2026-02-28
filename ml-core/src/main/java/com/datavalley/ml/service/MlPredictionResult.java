package com.datavalley.ml.service;

public record MlPredictionResult(
    String label,
    double confidence,
    String logitsJson
) {
}
