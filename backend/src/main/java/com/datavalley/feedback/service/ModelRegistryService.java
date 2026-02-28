package com.datavalley.feedback.service;

import com.datavalley.feedback.entity.ModelVersion;

import java.util.List;

public interface ModelRegistryService {
    ModelVersion getActiveModel();

    ModelVersion registerNewModel(String versionName, String artifactPath, String vocabPath, String configJson);

    ModelVersion activateModel(Long versionId);

    List<ModelVersion> listModels();
}
