package com.datavalley.feedback.controller;

import com.datavalley.feedback.dto.OverviewMetrics;
import com.datavalley.feedback.dto.TopicItem;
import com.datavalley.feedback.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public OverviewMetrics overview(@RequestParam(name = "from", required = false) LocalDate from,
                                    @RequestParam(name = "to", required = false) LocalDate to) {
        return analyticsService.getOverview(from, to);
    }

    @GetMapping("/topics")
    public List<TopicItem> topics(@RequestParam(name = "from", required = false) LocalDate from,
                                  @RequestParam(name = "to", required = false) LocalDate to) {
        return analyticsService.getTopics(from, to);
    }
}
