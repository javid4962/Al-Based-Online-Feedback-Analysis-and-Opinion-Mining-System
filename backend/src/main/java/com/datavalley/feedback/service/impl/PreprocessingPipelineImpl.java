package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.service.PreprocessingPipeline;
import com.datavalley.ml.pipeline.TextPreprocessor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreprocessingPipelineImpl implements PreprocessingPipeline {

    private final TextPreprocessor textPreprocessor = new TextPreprocessor();

    @Override
    public String normalize(String text) {
        return textPreprocessor.normalize(text);
    }

    @Override
    public List<String> tokenize(String text) {
        return textPreprocessor.tokenize(text);
    }

    @Override
    public double[] vectorize(List<String> tokens) {
        double[] vector = new double[Math.min(tokens.size(), 128)];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = Math.abs(tokens.get(i).hashCode() % 1000) / 1000.0;
        }
        return vector;
    }
}
