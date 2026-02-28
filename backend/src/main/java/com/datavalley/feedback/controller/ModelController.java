package com.datavalley.feedback.controller;

import com.datavalley.feedback.dto.ModelActivationResponse;
import com.datavalley.feedback.dto.ModelVersionResponse;
import com.datavalley.feedback.dto.TrainRequest;
import com.datavalley.feedback.dto.TrainingRunResponse;
import com.datavalley.feedback.entity.ModelVersion;
import com.datavalley.feedback.service.ModelRegistryService;
import com.datavalley.feedback.service.SentimentModelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/models")
public class ModelController {

    private final SentimentModelService sentimentModelService;
    private final ModelRegistryService modelRegistryService;

    public ModelController(SentimentModelService sentimentModelService,
                           ModelRegistryService modelRegistryService) {
        this.sentimentModelService = sentimentModelService;
        this.modelRegistryService = modelRegistryService;
    }

    @PostMapping("/train")
    public TrainingRunResponse train(@RequestBody TrainRequest request) {
        Map<String, Object> result = sentimentModelService.train(request);
        return new TrainingRunResponse(((Number) result.get("runId")).longValue(), String.valueOf(result.get("status")));
    }

    @GetMapping
    public List<ModelVersionResponse> listModels() {
        return modelRegistryService.listModels().stream()
            .map(model -> new ModelVersionResponse(model.getId(), model.getVersionName(), model.isActive(), model.getCreatedAt()))
            .toList();
    }

    @PostMapping("/{versionId}/activate")
    public ModelActivationResponse activate(@PathVariable("versionId") Long versionId) {
        ModelVersion active = modelRegistryService.activateModel(versionId);
        return new ModelActivationResponse(active.getId());
    }
}
