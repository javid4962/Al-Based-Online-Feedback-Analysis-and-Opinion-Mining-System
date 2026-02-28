package com.datavalley.feedback.service.impl;

import com.datavalley.feedback.dto.OverviewMetrics;
import com.datavalley.feedback.dto.TopicItem;
import com.datavalley.feedback.dto.TrendPoint;
import com.datavalley.feedback.entity.AnalyticsDaily;
import com.datavalley.feedback.entity.SentimentPrediction;
import com.datavalley.feedback.enums.SentimentLabel;
import com.datavalley.feedback.repository.AnalyticsDailyRepository;
import com.datavalley.feedback.repository.SentimentPredictionRepository;
import com.datavalley.feedback.service.AnalyticsService;
import com.datavalley.feedback.service.PreprocessingPipeline;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final SentimentPredictionRepository sentimentPredictionRepository;
    private final AnalyticsDailyRepository analyticsDailyRepository;
    private final PreprocessingPipeline preprocessingPipeline;

    public AnalyticsServiceImpl(SentimentPredictionRepository sentimentPredictionRepository,
                                AnalyticsDailyRepository analyticsDailyRepository,
                                PreprocessingPipeline preprocessingPipeline) {
        this.sentimentPredictionRepository = sentimentPredictionRepository;
        this.analyticsDailyRepository = analyticsDailyRepository;
        this.preprocessingPipeline = preprocessingPipeline;
    }

    @Override
    public OverviewMetrics getOverview(LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();

        Instant fromInstant = start.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<SentimentPrediction> predictions = sentimentPredictionRepository.findForSubmittedRange(fromInstant, toInstant);

        long positive = predictions.stream().filter(p -> p.getSentimentLabel() == SentimentLabel.POSITIVE).count();
        long negative = predictions.stream().filter(p -> p.getSentimentLabel() == SentimentLabel.NEGATIVE).count();
        long total = predictions.size();

        List<TrendPoint> trendSeries = buildTrendSeries(start, end);

        double positivePct = total == 0 ? 0.0 : (positive * 100.0) / total;
        double negativePct = total == 0 ? 0.0 : (negative * 100.0) / total;

        return new OverviewMetrics(total, positive, negative, positivePct, negativePct, trendSeries);
    }

    @Override
    public List<TopicItem> getTopics(LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();

        Instant fromInstant = start.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<SentimentPrediction> predictions = sentimentPredictionRepository.findForSubmittedRange(fromInstant, toInstant);
        Map<String, Long> pos = new HashMap<>();
        Map<String, Long> neg = new HashMap<>();

        for (SentimentPrediction prediction : predictions) {
            List<String> tokens = preprocessingPipeline.tokenize(prediction.getFeedbackItem().getTextRaw());
            Map<String, Long> target = prediction.getSentimentLabel() == SentimentLabel.POSITIVE ? pos : neg;
            for (String token : tokens) {
                if (token.length() < 3) {
                    continue;
                }
                target.merge(token, 1L, Long::sum);
            }
        }

        List<TopicItem> result = new ArrayList<>();
        pos.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> result.add(new TopicItem(entry.getKey(), entry.getValue(), "POSITIVE")));

        neg.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> result.add(new TopicItem(entry.getKey(), entry.getValue(), "NEGATIVE")));

        return result;
    }

    private List<TrendPoint> buildTrendSeries(LocalDate from, LocalDate to) {
        List<AnalyticsDaily> rows = analyticsDailyRepository.findByDateKeyBetween(from, to);
        if (rows.isEmpty()) {
            return List.of();
        }

        return rows.stream()
            .sorted(Comparator.comparing(AnalyticsDaily::getDateKey))
            .map(row -> new TrendPoint(row.getDateKey(), row.getTotalCount(), row.getPosCount(), row.getNegCount()))
            .toList();
    }
}
