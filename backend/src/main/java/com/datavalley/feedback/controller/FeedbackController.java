package com.datavalley.feedback.controller;

import com.datavalley.feedback.dto.FeedbackCreateRequest;
import com.datavalley.feedback.dto.FeedbackListItem;
import com.datavalley.feedback.dto.FeedbackResponse;
import com.datavalley.feedback.dto.JobResponse;
import com.datavalley.feedback.entity.FeedbackItem;
import com.datavalley.feedback.entity.SentimentPrediction;
import com.datavalley.feedback.enums.SentimentLabel;
import com.datavalley.feedback.enums.SourceType;
import com.datavalley.feedback.repository.FeedbackItemRepository;
import com.datavalley.feedback.repository.SentimentPredictionRepository;
import com.datavalley.feedback.service.FeedbackIngestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackIngestionService feedbackIngestionService;
    private final FeedbackItemRepository feedbackItemRepository;
    private final SentimentPredictionRepository sentimentPredictionRepository;
    private final long csvMaxBytes;

    public FeedbackController(FeedbackIngestionService feedbackIngestionService,
                              FeedbackItemRepository feedbackItemRepository,
                              SentimentPredictionRepository sentimentPredictionRepository,
                              @Value("${app.csv.max-bytes:5242880}") long csvMaxBytes) {
        this.feedbackIngestionService = feedbackIngestionService;
        this.feedbackItemRepository = feedbackItemRepository;
        this.sentimentPredictionRepository = sentimentPredictionRepository;
        this.csvMaxBytes = csvMaxBytes;
    }

    @PostMapping
    public FeedbackResponse createFeedback(@Valid @RequestBody FeedbackCreateRequest request) {
        return feedbackIngestionService.ingestSingle(request);
    }

    @PostMapping("/bulk-csv")
    public JobResponse bulkUpload(@RequestPart("file") MultipartFile file) throws Exception {
        validateCsvFile(file);
        Long jobId = feedbackIngestionService.ingestCsvAsync(file.getBytes());
        return new JobResponse(jobId, "RUNNING");
    }

    @GetMapping
    public Map<String, Object> listFeedback(
        @RequestParam(name = "source", required = false) SourceType source,
        @RequestParam(name = "sourceType", required = false) SourceType sourceType,
        @RequestParam(name = "sentiment", required = false) SentimentLabel sentiment,
        @RequestParam(name = "confidence", required = false) Double confidence,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "from", required = false) LocalDate from,
        @RequestParam(name = "to", required = false) LocalDate to,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > 200) {
            throw new IllegalArgumentException("size must be between 1 and 200");
        }
        if (confidence != null && (confidence < 0.0 || confidence > 1.0)) {
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }

        SourceType effectiveSource = source != null ? source : sourceType;

        Instant fromInstant = from != null
            ? from.atStartOfDay().toInstant(ZoneOffset.UTC)
            : Instant.EPOCH;
        Instant toInstant = to != null
            ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
            : Instant.now().plusSeconds(24 * 60 * 60L);

        List<FeedbackItem> candidates = feedbackItemRepository.findAllBySubmittedAtBetween(fromInstant, toInstant);
        if (effectiveSource != null) {
            candidates = candidates.stream()
                .filter(item -> item.getSourceType() == effectiveSource)
                .collect(Collectors.toList());
        }

        String keywordNormalized = keyword == null ? null : keyword.trim().toLowerCase();
        if (keywordNormalized != null && !keywordNormalized.isBlank()) {
            candidates = candidates.stream()
                .filter(item -> containsIgnoreCase(item.getTextRaw(), keywordNormalized)
                    || containsIgnoreCase(item.getSourceRef(), keywordNormalized))
                .collect(Collectors.toList());
        }

        Map<Long, SentimentPrediction> latestPredictions = latestPredictionByFeedbackId(candidates);

        List<FeedbackListItem> content = new ArrayList<>();
        for (FeedbackItem item : candidates.stream()
            .sorted(Comparator.comparing(FeedbackItem::getSubmittedAt).reversed())
            .toList()) {
            SentimentPrediction latest = latestPredictions.get(item.getId());
            SentimentLabel label = latest != null ? latest.getSentimentLabel() : null;
            if (sentiment != null && label != sentiment) {
                continue;
            }
            if (confidence != null && (latest == null || latest.getConfidence() < confidence)) {
                continue;
            }
            content.add(new FeedbackListItem(
                item.getId(),
                item.getSourceType(),
                item.getSourceRef(),
                item.getTextRaw(),
                item.getRating(),
                item.getLocale(),
                item.getSubmittedAt(),
                label,
                latest != null ? latest.getConfidence() : null
            ));
        }

        int totalElements = content.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<FeedbackListItem> pagedContent = fromIndex >= totalElements
            ? List.of()
            : content.subList(fromIndex, toIndex);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pagedContent);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalElements);
        response.put("totalPages", (int) Math.ceil(totalElements / (double) size));
        return response;
    }

    private Map<Long, SentimentPrediction> latestPredictionByFeedbackId(List<FeedbackItem> feedbackItems) {
        if (feedbackItems.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = feedbackItems.stream().map(FeedbackItem::getId).toList();
        return sentimentPredictionRepository.findByFeedbackItemIdIn(ids).stream()
            .sorted(Comparator.comparing(SentimentPrediction::getInferredAt).reversed())
            .collect(Collectors.toMap(
                prediction -> prediction.getFeedbackItem().getId(),
                prediction -> prediction,
                (existing, ignored) -> existing,
                LinkedHashMap::new
            ));
    }

    private void validateCsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV file must not be empty");
        }
        if (file.getSize() > csvMaxBytes) {
            throw new IllegalArgumentException("CSV file exceeds max size of " + csvMaxBytes + " bytes");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only .csv file uploads are supported");
        }
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
