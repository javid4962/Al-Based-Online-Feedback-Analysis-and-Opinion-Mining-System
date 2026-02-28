package com.datavalley.feedback.service;

import com.datavalley.feedback.dto.PredictionResultDto;
import com.datavalley.feedback.dto.TrainRequest;

import java.util.Map;

public interface SentimentModelService {
    PredictionResultDto predict(String text, String activeModelVersion);

    Map<String, Object> train(TrainRequest request);

    Map<String, Object> evaluate(String modelVersion);
}
