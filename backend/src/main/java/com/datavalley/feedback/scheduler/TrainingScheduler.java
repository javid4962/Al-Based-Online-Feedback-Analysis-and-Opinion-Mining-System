package com.datavalley.feedback.scheduler;

import com.datavalley.feedback.dto.TrainRequest;
import com.datavalley.feedback.service.SentimentModelService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrainingScheduler {

    private final SentimentModelService sentimentModelService;

    public TrainingScheduler(SentimentModelService sentimentModelService) {
        this.sentimentModelService = sentimentModelService;
    }

    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyRetrain() {
        TrainRequest request = new TrainRequest("AUTO_LABEL_ONLY", 100, 128, 0.001, true);
        sentimentModelService.train(request);
    }
}
