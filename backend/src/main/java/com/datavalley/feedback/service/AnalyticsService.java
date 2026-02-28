package com.datavalley.feedback.service;

import com.datavalley.feedback.dto.OverviewMetrics;
import com.datavalley.feedback.dto.TopicItem;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {
    OverviewMetrics getOverview(LocalDate from, LocalDate to);

    List<TopicItem> getTopics(LocalDate from, LocalDate to);
}
