package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.dto.JobResponse;
import com.datavalley.feedback.dto.PredictionResponse;
import com.datavalley.feedback.dto.PredictionResultDto;
import com.datavalley.feedback.dto.ReanalyzeRequest;
import com.datavalley.feedback.entity.AnalyticsDaily;
import com.datavalley.feedback.entity.FeedbackItem;
import com.datavalley.feedback.entity.FeedbackLabel;
import com.datavalley.feedback.entity.JobRun;
import com.datavalley.feedback.entity.ModelVersion;
import com.datavalley.feedback.entity.SentimentPrediction;
import com.datavalley.feedback.enums.JobStatus;
import com.datavalley.feedback.enums.JobType;
import com.datavalley.feedback.enums.LabelSource;
import com.datavalley.feedback.enums.SentimentLabel;
import com.datavalley.feedback.enums.SourceType;
import com.datavalley.feedback.repository.AnalyticsDailyRepository;
import com.datavalley.feedback.repository.FeedbackItemRepository;
import com.datavalley.feedback.repository.FeedbackLabelRepository;
import com.datavalley.feedback.repository.JobRunRepository;
import com.datavalley.feedback.repository.SentimentPredictionRepository;
import com.datavalley.feedback.service.AuditLogService;
import com.datavalley.feedback.service.ModelRegistryService;
import com.datavalley.feedback.service.PredictionService;
import com.datavalley.feedback.service.SentimentModelService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Executor;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final SentimentModelService sentimentModelService;
    private final ModelRegistryService modelRegistryService;
    private final SentimentPredictionRepository sentimentPredictionRepository;
    private final FeedbackLabelRepository feedbackLabelRepository;
    private final FeedbackItemRepository feedbackItemRepository;
    private final JobRunRepository jobRunRepository;
    private final AnalyticsDailyRepository analyticsDailyRepository;
    private final AuditLogService auditLogService;
    private final Executor jobExecutor;

    public PredictionServiceImpl(SentimentModelService sentimentModelService,
                                 ModelRegistryService modelRegistryService,
                                 SentimentPredictionRepository sentimentPredictionRepository,
                                 FeedbackLabelRepository feedbackLabelRepository,
                                 FeedbackItemRepository feedbackItemRepository,
                                 JobRunRepository jobRunRepository,
                                 AnalyticsDailyRepository analyticsDailyRepository,
                                 AuditLogService auditLogService,
                                 @Qualifier("jobExecutor") Executor jobExecutor) {
        this.sentimentModelService = sentimentModelService;
        this.modelRegistryService = modelRegistryService;
        this.sentimentPredictionRepository = sentimentPredictionRepository;
        this.feedbackLabelRepository = feedbackLabelRepository;
        this.feedbackItemRepository = feedbackItemRepository;
        this.jobRunRepository = jobRunRepository;
        this.analyticsDailyRepository = analyticsDailyRepository;
        this.auditLogService = auditLogService;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public void predictAndPersist(FeedbackItem feedbackItem) {
        ModelVersion activeModel = modelRegistryService.getActiveModel();
        String versionName = activeModel != null ? activeModel.getVersionName() : "bootstrap-v0";

        PredictionResultDto result = sentimentModelService.predict(feedbackItem.getTextRaw(), versionName);

        SentimentPrediction prediction = new SentimentPrediction();
        prediction.setFeedbackItem(feedbackItem);
        prediction.setModelVersion(activeModel);
        prediction.setSentimentLabel(result.label());
        prediction.setConfidence(result.confidence());
        prediction.setLogitsJson(result.logitsJson());
        prediction.setInferredAt(Instant.now());
        sentimentPredictionRepository.save(prediction);

        FeedbackLabel modelLabel = new FeedbackLabel();
        modelLabel.setFeedbackItem(feedbackItem);
        modelLabel.setLabelSource(LabelSource.MODEL);
        modelLabel.setSentimentLabel(result.label());
        modelLabel.setLabeledAt(Instant.now());
        feedbackLabelRepository.save(modelLabel);

        updateAnalyticsDaily(feedbackItem, result.label(), result.confidence());
    }

    @Override
    @Transactional(readOnly = true)
    public PredictionResponse getPrediction(Long feedbackId) {
        SentimentPrediction prediction = sentimentPredictionRepository.findTopByFeedbackItemIdOrderByInferredAtDesc(feedbackId)
            .orElseThrow(() -> new IllegalArgumentException("Prediction not found for feedback id " + feedbackId));

        String version = prediction.getModelVersion() != null ? prediction.getModelVersion().getVersionName() : "bootstrap-v0";

        return new PredictionResponse(
            feedbackId,
            prediction.getSentimentLabel(),
            prediction.getConfidence(),
            version,
            prediction.getInferredAt()
        );
    }

    @Override
    public JobResponse reanalyze(ReanalyzeRequest request) {
        validateReanalyzeRequest(request);

        JobRun job = new JobRun();
        job.setJobType(JobType.REANALYZE);
        job.setStatus(JobStatus.RUNNING);
        job.setProgress(0);
        job.setPayloadJson("{\"phase\":\"started\"}");
        job.setStartedAt(Instant.now());
        job = jobRunRepository.save(job);

        Long jobId = job.getId();
        jobExecutor.execute(() -> processReanalysis(jobId, request));
        return new JobResponse(jobId, JobStatus.RUNNING.name());
    }

    private void validateReanalyzeRequest(ReanalyzeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Reanalyze request is required");
        }
        if (request.fromDate() != null && request.toDate() != null && request.fromDate().isAfter(request.toDate())) {
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        }
        if (request.sourceType() != null && !request.sourceType().isBlank()) {
            SourceType.valueOf(request.sourceType().toUpperCase());
        }
    }

    private void processReanalysis(Long jobId, ReanalyzeRequest request) {
        JobRun job = jobRunRepository.findById(jobId).orElseThrow();
        try {
            Instant from = request.fromDate() != null
                ? request.fromDate().atStartOfDay().toInstant(ZoneOffset.UTC)
                : Instant.EPOCH;
            Instant to = request.toDate() != null
                ? request.toDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                : Instant.now();

            List<FeedbackItem> all = feedbackItemRepository.findAllBySubmittedAtBetween(from, to);
            List<FeedbackItem> filtered = all;
            if (request.sourceType() != null && !request.sourceType().isBlank()) {
                SourceType src = SourceType.valueOf(request.sourceType().toUpperCase());
                filtered = all.stream().filter(f -> f.getSourceType() == src).toList();
            }

            int total = filtered.size();
            int processed = 0;
            for (FeedbackItem item : filtered) {
                predictAndPersist(item);
                processed++;
                if (processed % 20 == 0 || processed == total) {
                    job.setProgress(total == 0 ? 100 : Math.round((processed * 100.0f) / total));
                    jobRunRepository.save(job);
                }
            }

            job.setStatus(JobStatus.COMPLETED);
            job.setProgress(100);
            job.setPayloadJson("{\"processed\":" + processed + ",\"total\":" + total + "}");
            job.setEndedAt(Instant.now());
            jobRunRepository.save(job);
            auditLogService.log("admin", "REANALYZE_COMPLETED", "JOB_RUN", jobId.toString(), job.getPayloadJson());
        } catch (Exception ex) {
            job.setStatus(JobStatus.FAILED);
            job.setPayloadJson("{\"error\":\"" + sanitize(ex.getMessage()) + "\"}");
            job.setEndedAt(Instant.now());
            jobRunRepository.save(job);
            auditLogService.log("admin", "REANALYZE_FAILED", "JOB_RUN", jobId.toString(), job.getPayloadJson());
        }
    }

    private void updateAnalyticsDaily(FeedbackItem feedbackItem, SentimentLabel label, double confidence) {
        LocalDate day = feedbackItem.getSubmittedAt().atZone(ZoneOffset.UTC).toLocalDate();
        AnalyticsDaily daily = analyticsDailyRepository
            .findByDateKeyAndSourceType(day, feedbackItem.getSourceType())
            .orElseGet(() -> {
                AnalyticsDaily created = new AnalyticsDaily();
                created.setDateKey(day);
                created.setSourceType(feedbackItem.getSourceType());
                created.setTotalCount(0);
                created.setPosCount(0);
                created.setNegCount(0);
                created.setAvgConfidence(0.0);
                return created;
            });

        int nextTotal = daily.getTotalCount() + 1;
        daily.setTotalCount(nextTotal);
        if (label == SentimentLabel.POSITIVE) {
            daily.setPosCount(daily.getPosCount() + 1);
        } else {
            daily.setNegCount(daily.getNegCount() + 1);
        }

        double prevAvg = daily.getAvgConfidence() == null ? 0.0 : daily.getAvgConfidence();
        double nextAvg = ((prevAvg * (nextTotal - 1)) + confidence) / nextTotal;
        daily.setAvgConfidence(nextAvg);

        analyticsDailyRepository.save(daily);
    }

    private String sanitize(String message) {
        if (message == null) {
            return "unknown_error";
        }
        return message.replace("\"", "'");
    }
}
