package com.datavalley.feedback.dto;

import java.util.List;

public record OverviewMetrics(
    long total,
    long positive,
    long negative,
    double positivePct,
    double negativePct,
    List<TrendPoint> trendSeries
) {
}
