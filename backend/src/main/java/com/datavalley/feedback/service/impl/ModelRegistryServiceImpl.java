package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.entity.ModelVersion;
import com.datavalley.feedback.repository.ModelVersionRepository;
import com.datavalley.feedback.service.AuditLogService;
import com.datavalley.feedback.service.ModelRegistryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ModelRegistryServiceImpl implements ModelRegistryService {

    private final ModelVersionRepository modelVersionRepository;
    private final AuditLogService auditLogService;

    public ModelRegistryServiceImpl(ModelVersionRepository modelVersionRepository,
                                    AuditLogService auditLogService) {
        this.modelVersionRepository = modelVersionRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public ModelVersion getActiveModel() {
        return modelVersionRepository.findByActiveTrue().orElse(null);
    }

    @Override
    @Transactional
    public ModelVersion registerNewModel(String versionName, String artifactPath, String vocabPath, String configJson) {
        ModelVersion version = new ModelVersion();
        version.setVersionName(versionName);
        version.setArtifactPath(artifactPath);
        version.setVocabPath(vocabPath);
        version.setConfigJson(configJson);
        version.setActive(modelVersionRepository.findByActiveTrue().isEmpty());
        version.setCreatedAt(Instant.now());

        ModelVersion saved = modelVersionRepository.save(version);
        auditLogService.log("system", "MODEL_REGISTER", "MODEL_VERSION", saved.getId().toString(),
            "{\"version\":\"" + saved.getVersionName() + "\"}");
        return saved;
    }

    @Override
    @Transactional
    public ModelVersion activateModel(Long versionId) {
        ModelVersion target = modelVersionRepository.findById(versionId)
            .orElseThrow(() -> new IllegalArgumentException("Model version not found"));

        modelVersionRepository.findByActiveTrue().ifPresent(active -> {
            active.setActive(false);
            modelVersionRepository.save(active);
        });

        target.setActive(true);
        ModelVersion saved = modelVersionRepository.save(target);
        auditLogService.log("admin", "MODEL_ACTIVATE", "MODEL_VERSION", saved.getId().toString(), "{}");
        return saved;
    }

    @Override
    public List<ModelVersion> listModels() {
        return modelVersionRepository.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .toList();
    }
}
