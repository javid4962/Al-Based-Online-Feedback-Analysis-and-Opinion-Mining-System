package com.datavalley.ml.service;

import java.util.Map;

public record MlTrainingResult(
    String versionName,
    String artifactPath,
    String vocabPath,
    Map<String, Object> metrics
) {
}
