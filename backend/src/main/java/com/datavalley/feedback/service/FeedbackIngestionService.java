package com.datavalley.feedback.service;

import com.datavalley.feedback.dto.FeedbackCreateRequest;
import com.datavalley.feedback.dto.FeedbackResponse;

public interface FeedbackIngestionService {
    FeedbackResponse ingestSingle(FeedbackCreateRequest request);

    Long ingestCsvAsync(byte[] csvBytes);
}
