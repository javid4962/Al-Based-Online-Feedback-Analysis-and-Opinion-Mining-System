package com.datavalley.feedback.service;

import com.datavalley.feedback.dto.JobResponse;
import com.datavalley.feedback.dto.PredictionResponse;
import com.datavalley.feedback.dto.ReanalyzeRequest;
import com.datavalley.feedback.entity.FeedbackItem;

public interface PredictionService {
    void predictAndPersist(FeedbackItem feedbackItem);

    PredictionResponse getPrediction(Long feedbackId);

    JobResponse reanalyze(ReanalyzeRequest request);
}
