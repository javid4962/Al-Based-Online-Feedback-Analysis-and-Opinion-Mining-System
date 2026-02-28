package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.dto.FeedbackCreateRequest;
import com.datavalley.feedback.dto.FeedbackResponse;
import com.datavalley.feedback.entity.FeedbackItem;
import com.datavalley.feedback.entity.FeedbackLabel;
import com.datavalley.feedback.entity.JobRun;
import com.datavalley.feedback.enums.JobStatus;
import com.datavalley.feedback.enums.JobType;
import com.datavalley.feedback.enums.LabelSource;
import com.datavalley.feedback.enums.SentimentLabel;
import com.datavalley.feedback.enums.SourceType;
import com.datavalley.feedback.repository.FeedbackItemRepository;
import com.datavalley.feedback.repository.FeedbackLabelRepository;
import com.datavalley.feedback.repository.JobRunRepository;
import com.datavalley.feedback.service.AuditLogService;
import com.datavalley.feedback.service.FeedbackIngestionService;
import com.datavalley.feedback.service.PredictionService;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Executor;

@Service
public class FeedbackIngestionServiceImpl implements FeedbackIngestionService {

    private final FeedbackItemRepository feedbackItemRepository;
    private final FeedbackLabelRepository feedbackLabelRepository;
    private final JobRunRepository jobRunRepository;
    private final PredictionService predictionService;
    private final AuditLogService auditLogService;
    private final Executor jobExecutor;

    public FeedbackIngestionServiceImpl(FeedbackItemRepository feedbackItemRepository,
                                        FeedbackLabelRepository feedbackLabelRepository,
                                        JobRunRepository jobRunRepository,
                                        PredictionService predictionService,
                                        AuditLogService auditLogService,
                                        @Qualifier("jobExecutor") Executor jobExecutor) {
        this.feedbackItemRepository = feedbackItemRepository;
        this.feedbackLabelRepository = feedbackLabelRepository;
        this.jobRunRepository = jobRunRepository;
        this.predictionService = predictionService;
        this.auditLogService = auditLogService;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public FeedbackResponse ingestSingle(FeedbackCreateRequest request) {
        if (request.text() == null || request.text().isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        if (request.rating() != null && (request.rating() < 1 || request.rating() > 5)) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }

        FeedbackItem item = new FeedbackItem();
        item.setSourceType(request.sourceType());
        item.setSourceRef(request.sourceRef());
        item.setTextRaw(request.text());
        item.setRating(request.rating());
        item.setLocale(request.locale());
        item.setSubmittedAt(request.submittedAt() != null ? request.submittedAt() : Instant.now());
        item.setIngestedAt(Instant.now());

        FeedbackItem saved = feedbackItemRepository.save(item);
        applyAutoLabelIfEligible(saved);
        predictionService.predictAndPersist(saved);

        auditLogService.log("admin", "FEEDBACK_INGEST", "FEEDBACK_ITEM", saved.getId().toString(), "{}");
        return new FeedbackResponse(saved.getId(), "ACCEPTED");
    }

    @Override
    public Long ingestCsvAsync(byte[] csvBytes) {
        JobRun job = new JobRun();
        job.setJobType(JobType.INGEST);
        job.setStatus(JobStatus.RUNNING);
        job.setProgress(0);
        job.setPayloadJson("{\"phase\":\"started\"}");
        job.setStartedAt(Instant.now());
        job = jobRunRepository.save(job);

        Long jobId = job.getId();
        jobExecutor.execute(() -> processCsv(jobId, csvBytes));
        return jobId;
    }

    private void processCsv(Long jobId, byte[] csvBytes) {
        JobRun job = jobRunRepository.findById(jobId).orElseThrow();
        int success = 0;
        int failed = 0;

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(csvBytes), StandardCharsets.UTF_8))) {
            String[] row;
            int rowNum = 0;
            while ((row = csvReader.readNext()) != null) {
                rowNum++;
                if (rowNum == 1 && isLikelyHeader(row)) {
                    continue;
                }

                try {
                    FeedbackCreateRequest request = parseRow(row);
                    ingestSingle(request);
                    success++;
                } catch (Exception rowEx) {
                    failed++;
                }

                int processed = success + failed;
                if (processed % 20 == 0) {
                    job.setProgress(Math.min(99, processed));
                    jobRunRepository.save(job);
                }
            }

            job.setStatus(JobStatus.COMPLETED);
            job.setProgress(100);
            job.setPayloadJson("{\"success\":" + success + ",\"failed\":" + failed + "}");
            job.setEndedAt(Instant.now());
            jobRunRepository.save(job);
            auditLogService.log("admin", "CSV_INGEST_COMPLETED", "JOB_RUN", jobId.toString(), job.getPayloadJson());
        } catch (Exception ex) {
            job.setStatus(JobStatus.FAILED);
            job.setPayloadJson("{\"error\":\"" + sanitize(ex.getMessage()) + "\"}");
            job.setEndedAt(Instant.now());
            jobRunRepository.save(job);
            auditLogService.log("admin", "CSV_INGEST_FAILED", "JOB_RUN", jobId.toString(), job.getPayloadJson());
        }
    }

    private void applyAutoLabelIfEligible(FeedbackItem item) {
        if (item.getRating() == null) {
            return;
        }

        SentimentLabel label;
        if (item.getRating() <= 2) {
            label = SentimentLabel.NEGATIVE;
        } else if (item.getRating() >= 4) {
            label = SentimentLabel.POSITIVE;
        } else {
            return;
        }

        FeedbackLabel feedbackLabel = new FeedbackLabel();
        feedbackLabel.setFeedbackItem(item);
        feedbackLabel.setLabelSource(LabelSource.AUTO_RULE);
        feedbackLabel.setSentimentLabel(label);
        feedbackLabel.setLabeledAt(Instant.now());
        feedbackLabelRepository.save(feedbackLabel);
    }

    private boolean isLikelyHeader(String[] row) {
        String joined = String.join(",", row).toLowerCase();
        return joined.contains("text") || joined.contains("rating") || joined.contains("source");
    }

    private FeedbackCreateRequest parseRow(String[] row) {
        if (row.length == 0) {
            throw new IllegalArgumentException("CSV row is empty");
        }

        if (row.length == 1) {
            String text = safe(row, 0);
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("CSV text is required");
            }
            return new FeedbackCreateRequest(SourceType.CSV, null, text, null, "en", Instant.now());
        }

        String sourceRef = safe(row, 0);
        String text = safe(row, 1);
        Integer rating = parseInteger(safe(row, 2));
        String locale = safe(row, 3) == null ? "en" : safe(row, 3);
        Instant submittedAt = parseInstant(safe(row, 4));

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("CSV text is required");
        }
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("CSV rating must be between 1 and 5");
        }

        return new FeedbackCreateRequest(SourceType.CSV, sourceRef, text, rating, locale, submittedAt);
    }

    private String safe(String[] row, int index) {
        if (index >= row.length) {
            return null;
        }
        String value = row[index];
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value);
    }

    private Instant parseInstant(String value) {
        if (value == null) {
            return Instant.now();
        }
        return Instant.parse(value);
    }

    private String sanitize(String value) {
        if (value == null) {
            return "unknown_error";
        }
        return value.replace("\"", "'");
    }
}
