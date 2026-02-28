package com.datavalley.feedback.controller;

import com.datavalley.feedback.dto.JobResponse;
import com.datavalley.feedback.dto.PredictionResponse;
import com.datavalley.feedback.dto.ReanalyzeRequest;
import com.datavalley.feedback.service.PredictionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/{feedbackId}")
    public PredictionResponse getPrediction(@PathVariable("feedbackId") Long feedbackId) {
        return predictionService.getPrediction(feedbackId);
    }

    @PostMapping("/reanalyze")
    public JobResponse reanalyze(@RequestBody ReanalyzeRequest request) {
        return predictionService.reanalyze(request);
    }
}
