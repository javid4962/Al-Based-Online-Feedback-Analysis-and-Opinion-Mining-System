package com.datavalley.feedback.entity;

import com.datavalley.feedback.enums.SourceType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "analytics_daily")
public class AnalyticsDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_key", nullable = false)
    private LocalDate dateKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "pos_count", nullable = false)
    private int posCount;

    @Column(name = "neg_count", nullable = false)
    private int negCount;

    @Column(name = "avg_confidence")
    private Double avgConfidence;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateKey() {
        return dateKey;
    }

    public void setDateKey(LocalDate dateKey) {
        this.dateKey = dateKey;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPosCount() {
        return posCount;
    }

    public void setPosCount(int posCount) {
        this.posCount = posCount;
    }

    public int getNegCount() {
        return negCount;
    }

    public void setNegCount(int negCount) {
        this.negCount = negCount;
    }

    public Double getAvgConfidence() {
        return avgConfidence;
    }

    public void setAvgConfidence(Double avgConfidence) {
        this.avgConfidence = avgConfidence;
    }
}
