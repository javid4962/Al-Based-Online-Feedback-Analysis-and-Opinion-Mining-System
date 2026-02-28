package com.datavalley.feedback.repository;

import com.datavalley.feedback.entity.AnalyticsDaily;
import com.datavalley.feedback.enums.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AnalyticsDailyRepository extends JpaRepository<AnalyticsDaily, Long> {
    List<AnalyticsDaily> findByDateKeyBetween(LocalDate from, LocalDate to);

    Optional<AnalyticsDaily> findByDateKeyAndSourceType(LocalDate dateKey, SourceType sourceType);
}
