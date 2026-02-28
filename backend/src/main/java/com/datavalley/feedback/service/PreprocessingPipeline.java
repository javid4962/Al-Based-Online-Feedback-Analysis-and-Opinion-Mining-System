package com.datavalley.feedback.service;

import java.util.List;

public interface PreprocessingPipeline {
    String normalize(String text);

    List<String> tokenize(String text);

    double[] vectorize(List<String> tokens);
}
